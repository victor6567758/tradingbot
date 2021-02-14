package com.tradebot.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.core.marketdata.historic.HistoricMarketDataProvider;
import com.tradebot.core.utils.CommonConsts;
import com.tradebot.response.CandleResponse;
import com.tradebot.response.GridContextResponse;
import com.tradebot.response.websocket.DataResponseMessage;
import com.tradebot.util.GeneralConst;
import com.tradebot.model.InitialContext;
import com.tradebot.model.TradingContext;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

@Service
@Slf4j
public class BitmexTradingBot extends BitmexTradingBotBase {

    private final ModelMapper modelMapper;
    private final Map<TradeableInstrument, TradingContext> tradingContextMap = new HashMap<>();
    private final Cache<Long, TradingContext> tradingContextCache;
    private final ReadWriteLock tradingContextMapLock = new ReentrantReadWriteLock();
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BitmexOrderManager bitmexOrderManager;
    private final AtomicBoolean tradesEnabled = new AtomicBoolean(false);

    private Map<TradeableInstrument, InitialContext> initialContextMap;

    public BitmexTradingBot(
        EventBus eventBus,
        ModelMapper modelMapper,
        SimpMessagingTemplate simpMessagingTemplate,
        BitmexOrderManager bitmexOrderManager,
        InstrumentService instrumentService,
        AccountDataProvider<Long> accountDataProvider,
        HistoricMarketDataProvider historicMarketDataProvider) {
        super(eventBus, instrumentService, accountDataProvider, historicMarketDataProvider);
        tradingContextCache = CacheBuilder.newBuilder()
            .expireAfterWrite(
                bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getTradingSolutionsDepthMin(),
                TimeUnit.MINUTES).build();
        this.modelMapper = modelMapper;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.bitmexOrderManager = bitmexOrderManager;
    }

    @PostConstruct
    public void initialize() {
        super.initialize();

        Account<Long> account = accountDataProvider.getLatestAccountInfo(bitmexAccountConfiguration.getBitmex()
            .getTradingConfiguration().getAccountId());
        bitmexOrderManager.initialize(account.getAccountId(), bitmexAccountConfiguration);

        initializeDetails();
        initializeModelMapper();
    }

    @PreDestroy
    public void deinitialize() {
        super.deinitialize();
    }

    public Map<String, GridContextResponse> getLastContextList() {
        tradingContextMapLock.readLock().lock();
        try {
            return tradingContextMap.entrySet().stream()
                .map(entry -> new ImmutablePair<>(entry.getKey().getInstrument(),
                    modelMapper.map(entry.getValue(), GridContextResponse.class)))
                .collect(Collectors.toUnmodifiableMap(
                    ImmutablePair::getLeft,
                    ImmutablePair::getRight));
        } finally {
            tradingContextMapLock.readLock().unlock();
        }
    }

    public Optional<GridContextResponse> getLastContextList(String symbol) {
        tradingContextMapLock.readLock().lock();
        try {
            TradingContext tradingContext = tradingContextMap.get(instrumentService.resolveTradeableInstrument(symbol));
            if (tradingContext == null) {
                return Optional.empty();
            }
            return Optional.of(modelMapper.map(tradingContext, GridContextResponse.class));
        } finally {
            tradingContextMapLock.readLock().unlock();
        }
    }

    public Set<GridContextResponse> getContextHistory(String symbol) {

        return tradingContextCache.asMap().values().stream()
            .filter(entry -> entry.getTradeableInstrument().getInstrument().equals(symbol))
            .map(context -> modelMapper.map(context, GridContextResponse.class))
            .collect(
                Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparingLong(value -> value.getCandleResponse().getDateTime()))
                ));
    }

    public Set<GridContextResponse> getContextHistory() {
        return tradingContextCache.asMap().values().stream()
            .map(tradingContext -> modelMapper.map(tradingContext, GridContextResponse.class))
            .collect(
                Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparingLong(value -> value.getCandleResponse().getDateTime()))
                ));
    }

    public Set<String> getAllSymbols() {
        return super.instruments.keySet().stream().map(TradeableInstrument::getInstrument).collect(Collectors.toSet());
    }

    public boolean setTradesEnabled(boolean tradesEnabledFlag) {
        return tradesEnabled.getAndSet(tradesEnabledFlag);
    }

    public void resetTradingContext() {
        tradingContextMapLock.writeLock().lock();
        try {
            tradingContextMap.clear();
        } finally {
            tradingContextMapLock.writeLock().unlock();
        }
    }

    @Override
    public void onTradeSolution(CandleStick candleStick, CacheCandlestick cacheCandlestick) {
        if (candleStick.getCandleGranularity() == CandleStickGranularity.M1) {
            log.info("Trade solution detected on this candle {}", candleStick.toString());
        }

        InitialContext initialContext = initialContextMap.get(candleStick.getInstrument());
        if (initialContext == null) {
            throw new IllegalArgumentException(String.format("Cannot find symbol %s", candleStick.getInstrument()));
        }

        Account<Long> account = accountDataProvider.getLatestAccountInfo(bitmexAccountConfiguration.getBitmex()
            .getTradingConfiguration().getAccountId());

        tradingContextMapLock.writeLock().lock();
        try {
            TradingContext tradingContext = tradingContextMap.get(candleStick.getInstrument());
            if (tradingContext == null) {
                tradingContext = new TradingContext(candleStick.getInstrument());

                calculateInitialContextParameters(account, candleStick, initialContext, tradingContext);
                tradingContextMap.put(candleStick.getInstrument(), tradingContext);

                if (log.isDebugEnabled()) {
                    log.debug("Trading initial setup is done: {}", tradingContext.toString());
                }
            }

            calculateVarContextParameters(account, candleStick, initialContext, tradingContext);
            tradingContextCache.put(System.currentTimeMillis(), tradingContext);

            bitmexOrderManager.onCandleCallback(candleStick, cacheCandlestick, initialContext, tradingContext);
            sendTradeConfig(tradingContext);

        } finally {
            tradingContextMapLock.writeLock().unlock();
        }

    }

    @Override
    public void onTradeSolution(Price price, Cache<DateTime, Price> instrumentRecentPricesCache) {
        // no-op
    }

    @Override
    public void visit(BitmexExecutionEventPayload event) {
        TradeableInstrument instrument =
            instrumentService.resolveTradeableInstrument(event.getPayLoad().getSymbol());

        tradingContextMapLock.writeLock().lock();
        try {
            TradingContext tradingContext = tradingContextMap.get(instrument);
            bitmexOrderManager.onOrderExecutionCallback(tradingContext, event);
        } finally {
            tradingContextMapLock.writeLock().unlock();
        }

    }

    @Override
    public void visit(BitmexOrderEventPayload event) {
        TradeableInstrument instrument =
            instrumentService.resolveTradeableInstrument(event.getPayLoad().getSymbol());
        tradingContextMapLock.writeLock().lock();
        try {
            TradingContext tradingContext = tradingContextMap.get(instrument);
            bitmexOrderManager.onOrderCallback(tradingContext, event);
        } finally {
            tradingContextMapLock.writeLock().unlock();
        }

    }

    private void calculateInitialContextParameters(
        Account<Long> account,
        CandleStick candleStick,
        InitialContext initialContext,
        TradingContext tradingContext) {
        double currentPrice = candleStick.getClosePrice() + candleStick.getClosePrice() / 100;
        double priceStep = (initialContext.getPriceEnd() - currentPrice) / initialContext.getLinesNum();

        for (int i = 0; i < initialContext.getLinesNum(); i++) {

            tradingContext.getTradingGrid().put(BigDecimal.valueOf(currentPrice),
                TradingDecision.builder().instrument(candleStick.getInstrument())
                    .signal(TradingSignal.LONG)
                    .limitPrice(currentPrice)
                    .stopPrice(0.0)
                    .units(initialContext.getOrderPosUnits())
                    .stopLossPrice(CommonConsts.INVALID_PRICE)
                    .takeProfitPrice(CommonConsts.INVALID_PRICE)
                    .build());

            currentPrice += priceStep;
        }

    }

    private void calculateVarContextParameters(
        Account<Long> account,
        CandleStick candleStick,
        InitialContext initialContext,
        TradingContext tradingContext) {
        tradingContext.setCandleStick(candleStick);

        if (StringUtils.isEmpty(initialContext.getReportExchangePair())) {
            initialContext.setReportExchangePair(account.getCurrency() + initialContext.getReportCurrency());
            log.info("Exchange pair detected to calculate margin {}", initialContext.getReportExchangePair());
        }
        tradingContext.setOneLotPrice(account.getTotalBalance().doubleValue() / initialContext.getLinesNum());


        if (!tradesEnabled.get()) {
            log.debug("Trades are globally disabled");
            return;
        }

        if (tradingContext.isTradeEnabled()) {
            //if (tradingContext.getOneLotPrice() > 1.0) {
            //    tradingContext.setTradeEnabled(false);
            //    log.info("One lot price for {} is more than 1.0, disabling trading",
            //        tradingContext.getTradeableInstrument().getInstrument());
            //} else {
            if (!tradingContext.isStarted()) {
                tradingContext.setStarted(true);

                log.info("Trading setup has started for {}", tradingContext.toString());
                bitmexOrderManager.startOrderEvolution(initialContext, tradingContext);
            }
            //}
        }
        {
            if (log.isDebugEnabled()) {
                log.debug("Trading is disabled for {}", tradingContext.getTradeableInstrument().getInstrument());
            }
        }
    }

    private void initializeDetails() {
        initialContextMap = algParameters.entrySet()
            .stream()
            .map(
                entry -> new ImmutablePair<>(entry.getKey(),
                    new InitialContext(
                        (Double) entry.getValue().get("x"),
                        (Double) entry.getValue().get("priceEnd"),
                        (Integer) entry.getValue().get("linesNum"),
                        (Integer) entry.getValue().get("orderPosUnits"),
                        (String) entry.getValue().get("reportCurrency")
                    ))
            ).collect(Collectors.toUnmodifiableMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight));
    }

    private void sendTradeConfig(TradingContext tradingContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Sending trading context to WS clients {}", tradingContext.toString());
            }
            simpMessagingTemplate.convertAndSend(GeneralConst.WS_TOPIC + GeneralConst.WS_TOPIC_PUBLISH_TRADE_CONFIG,
                new DataResponseMessage<>(modelMapper.map(tradingContext, GridContextResponse.class)));
        } catch (MessagingException messagingException) {
            log.error("Error sending data to websockets", messagingException);
        }
    }


    private void initializeModelMapper() {
        modelMapper.getConfiguration()
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        Converter<CandleStick, CandleResponse> candleStickConverter = context -> {
            CandleStick candleStick = context.getSource();
            return new CandleResponse(
                candleStick.getOpenPrice(),
                candleStick.getHighPrice(),
                candleStick.getLowPrice(),
                candleStick.getClosePrice(),
                candleStick.getEventDate().getMillis()
            );
        };
        Converter<TradeableInstrument, String> locationCodeConverter = context -> context.getSource().getInstrument();
        Converter<Map<BigDecimal, TradingDecision>, Set<Double>> tradeDecisionMapConverter =
            context -> {
                Map<BigDecimal, TradingDecision> source = context.getSource();
                return source != null ? source.keySet().stream().map(BigDecimal::doubleValue)
                    .collect(Collectors.toCollection(TreeSet::new)) : null;
            };

        modelMapper.addMappings(new PropertyMap<TradingContext, GridContextResponse>() {
            protected void configure() {
                using(locationCodeConverter).map(source.getTradeableInstrument()).setSymbol(null);
                using(tradeDecisionMapConverter).map(source.getTradingGrid()).setMesh(null);
                using(candleStickConverter).map(source.getCandleStick()).setCandleResponse(null);
            }
        });
    }


}
