package com.tradebot.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
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
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.core.utils.CommonConsts;
import com.tradebot.model.ImmutableTradingContext;
import com.tradebot.model.RecalculatedTradingContext;
import com.tradebot.model.TradingContext;
import com.tradebot.model.TradingDecisionContext;
import com.tradebot.response.CandleResponse;
import com.tradebot.response.ExecutionResponse;
import com.tradebot.response.GridContextResponse;
import com.tradebot.response.LimitResponse;
import com.tradebot.response.MeshEntry;
import com.tradebot.response.websocket.DataResponseMessage;
import com.tradebot.util.GeneralConst;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BitmexTradingBot extends BitmexTradingBotBase implements TradingBotRestApi {

  private final ModelMapper modelMapper;

  private final ReadWriteLock tradingContextMapLock = new ReentrantReadWriteLock();
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final BitmexOrderManager bitmexOrderManager;
  private final AtomicBoolean tradesEnabled = new AtomicBoolean(false);
  private final Cache<Long, GridContextResponse> tradingContextCache;

  private Map<TradeableInstrument, TradingContext> tradingContextMap;

  public BitmexTradingBot(
      EventBus eventBus,
      ModelMapper modelMapper,
      SimpMessagingTemplate simpMessagingTemplate,
      @Lazy BitmexOrderManager bitmexOrderManager,
      InstrumentService instrumentService,
      AccountDataProvider<Long> accountDataProvider,
      HistoricMarketDataProvider historicMarketDataProvider) {
    super(eventBus, instrumentService, accountDataProvider, historicMarketDataProvider);
    tradingContextCache = CacheBuilder.newBuilder()
        .expireAfterWrite(
            bitmexAccountConfiguration.getBitmex().getTradingConfiguration()
                .getTradingSolutionsDepthMin(),
            TimeUnit.MINUTES).build();
    this.modelMapper = modelMapper;
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.bitmexOrderManager = bitmexOrderManager;
  }

  @PostConstruct
  public void initialize() {
    super.initialize();

    Account<Long> account = accountDataProvider
        .getLatestAccountInfo(bitmexAccountConfiguration.getBitmex()
            .getTradingConfiguration().getAccountId());
    bitmexOrderManager.initialize(account.getAccountId(), bitmexAccountConfiguration);

    initializeDetails();
    initializeModelMapper();
  }

  @PreDestroy
  public void deinitialize() {
    super.deinitialize();
  }

  @Override
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

  @Override
  public Optional<GridContextResponse> getLastContextList(String symbol) {
    tradingContextMapLock.readLock().lock();
    try {
      TradingContext tradingContext = tradingContextMap
          .get(instrumentService.resolveTradeableInstrument(symbol));
      if (tradingContext == null) {
        return Optional.empty();
      }
      return Optional.of(modelMapper.map(tradingContext, GridContextResponse.class));
    } finally {
      tradingContextMapLock.readLock().unlock();
    }
  }

  @Override
  public Set<GridContextResponse> getContextHistory(String symbol) {

    return tradingContextCache.asMap().values().stream()
        .filter(entry -> entry.getSymbol().equals(symbol))
        .collect(
            Collectors.toCollection(
                () -> new TreeSet<>(
                    Comparator.comparingLong(GridContextResponse::getDateTime))
            ));
  }


  @Override
  public Set<String> getAllSymbols() {
    return super.instruments.keySet().stream().map(TradeableInstrument::getInstrument)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean setGlobalTradesEnabled(boolean tradesEnabledFlag) {
    log.info("Global trade status set: {}", tradesEnabledFlag);
    return tradesEnabled.getAndSet(tradesEnabledFlag);
  }

  @Override
  public List<String> cancelAllPendingOrders() {
    Collection<Order<String>> orders = bitmexOrderManager.cancelAllPendingOrders();
    return orders.stream().map(Order::getOrderId).collect(Collectors.toList());
  }

  @Override
  public void resetTradingContext() {
    tradingContextMapLock.writeLock().lock();
    try {
      tradingContextMap.values()
          .forEach(value -> value.setRecalculatedTradingContext(new RecalculatedTradingContext()));
    } finally {
      tradingContextMapLock.writeLock().unlock();
    }
  }

  @Override
  public Collection<CandleResponse> getCandleStickHistory(String symbol) {
    TradeableInstrument instrument =
        instrumentService.resolveTradeableInstrument(symbol);

    return historicMarketDataProvider.getCandleSticks(instrument, CandleStickGranularity.M1,
        bitmexAccountConfiguration.getBitmex().getApi().getHistoryDepth()).stream()
        .map(entry -> new CandleResponse(symbol, entry.getOpenPrice(), entry.getHighPrice(),
            entry.getLowPrice(), entry.getClosePrice(),
            entry.getEventDate().getMillis()))

        .collect(Collector.of(
            ArrayDeque::new,
            ArrayDeque::addFirst,
            (d1, d2) -> {
              d2.addAll(d1);
              return d2;
            }));
  }


  @Override
  public void onTradeSolution(CandleStick candleStick, CacheCandlestick cacheCandlestick) {
    if (candleStick.getCandleGranularity() == CandleStickGranularity.M1) {
      log.info("Trade solution detected on this candle {}", candleStick.toString());
    }

    Account<Long> account = accountDataProvider
        .getLatestAccountInfo(bitmexAccountConfiguration.getBitmex()
            .getTradingConfiguration().getAccountId());

    tradingContextMapLock.writeLock().lock();
    try {
      TradingContext tradingContext = tradingContextMap.get(candleStick.getInstrument());
      if (tradingContext == null) {
        throw new IllegalArgumentException(
            String.format("Cannot find symbol %s", candleStick.getInstrument()));
      }

      if (!tradingContext.getRecalculatedTradingContext().isOneTimeInitialized()) {
        tradingContext.getRecalculatedTradingContext().setOneTimeInitialized(true);
        calculateInitialContextParameters(account, candleStick, tradingContext);
        if (log.isDebugEnabled()) {
          log.debug("Initial context setup is done: {}", tradingContext.toString());
        }
      }

      calculateVarContextParameters(account, candleStick, tradingContext);

      tradingContextCache.put(System.currentTimeMillis(),
          modelMapper.map(tradingContext, GridContextResponse.class));
      bitmexOrderManager.onCandleCallback(candleStick, cacheCandlestick, tradingContext);

      sendTradeConfig(tradingContext);


    } finally {
      tradingContextMapLock.writeLock().unlock();
    }

    sendTradeCharts(candleStick);

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

      sendTradeConfig(tradingContext);
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

      sendTradeConfig(tradingContext);
    } finally {
      tradingContextMapLock.writeLock().unlock();
    }
  }

  @Override
  public void onOrderResult(OrderResultContext<String> orderResultContext) {
    TradeableInstrument instrument =
        instrumentService.resolveTradeableInstrument(orderResultContext.getSymbol());

    tradingContextMapLock.writeLock().lock();
    try {
      TradingContext tradingContext = tradingContextMap.get(instrument);

      if (orderResultContext instanceof BitmexOrderQuotas) {
        BitmexOrderQuotas bitmexOrderQuotas = (BitmexOrderQuotas) orderResultContext;
        tradingContext.getRecalculatedTradingContext().setBitmexOrderQuotas(bitmexOrderQuotas);
        sendBitmexLimitResponse(bitmexOrderQuotas);
        if (log.isDebugEnabled()) {
          log.debug("Order result (order quotas) callback {}", bitmexOrderQuotas.toString());
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Order result callback {}", orderResultContext.toString());
        }
      }

      bitmexOrderManager.onOrderResultCallback(tradingContext, orderResultContext);

    } finally {
      tradingContextMapLock.writeLock().unlock();
    }
  }


  @SneakyThrows
  private void calculateInitialContextParameters(
      Account<Long> account,
      CandleStick candleStick,
      TradingContext tradingContext) {
    double currentPrice = candleStick.getClosePrice() + candleStick.getClosePrice() / tradingContext
        .getImmutableTradingContext().getCurrentPriceAdjustDivider();
    double priceStep = (tradingContext.getImmutableTradingContext().getPriceEnd() - currentPrice) /
        tradingContext.getImmutableTradingContext().getLinesNum();

    tradingContext.getRecalculatedTradingContext().setProfitPlus(
        Math.abs(priceStep) * tradingContext.getImmutableTradingContext().getXPct()
    );

    for (int i = 0; i < tradingContext.getImmutableTradingContext().getLinesNum(); i++) {
      double roundedPrice = BitmexUtils.roundPrice(candleStick.getInstrument(), currentPrice);

      tradingContext.getRecalculatedTradingContext().getOpenTradingDecisions().put(i,
          TradingDecision.<TradingDecisionContext>builder().instrument(candleStick.getInstrument())
              .signal(TradingSignal.LONG)
              .limitPrice(roundedPrice)
              .stopPrice(0.0)
              .units(tradingContext.getImmutableTradingContext().getOrderPosUnits())
              .stopLossPrice(CommonConsts.INVALID_PRICE)
              .takeProfitPrice(CommonConsts.INVALID_PRICE)
              .context(new TradingDecisionContext(i))
              .build());

      tradingContext.getRecalculatedTradingContext().getExecutionChains().put(i, new ArrayList<>());
      currentPrice += priceStep;
    }


  }

  private void calculateVarContextParameters(
      Account<Long> account,
      CandleStick candleStick,
      TradingContext tradingContext) {
    tradingContext.getRecalculatedTradingContext().setCandleStick(candleStick);

    if (!tradesEnabled.get()) {
      log.debug("Trades are globally disabled");
      return;
    }

    if (!tradingContext.getRecalculatedTradingContext().isOrdersProcessingStarted()) {
      tradingContext.getRecalculatedTradingContext().setOrdersProcessingStarted(true);

      log.info("Trading setup has started for {}", tradingContext.toString());
      bitmexOrderManager.startOrderEvolution(tradingContext);
    }

  }

  private void initializeDetails() {
    try {
      tradingContextMap = algParameters.entrySet()
          .stream()
          .map(
              entry -> new ImmutablePair<>(
                  entry.getKey(),
                  new TradingContext(ImmutableTradingContext.builder()
                      .xPct((Double) entry.getValue().get("xPct"))
                      .currentPriceAdjustDivider(
                          (Double) entry.getValue().get("currentPriceAdjustDivider"))
                      .priceEnd((Double) entry.getValue().get("priceEnd"))
                      .linesNum((Integer) entry.getValue().get("linesNum"))
                      .orderPosUnits((Integer) entry.getValue().get("orderPosUnits"))
                      .reportCurrency((String) entry.getValue().get("reportCurrency"))
                      .tradeableInstrument(entry.getKey())
                      .reportExchangePair(
                          createReportExchangePair((String) entry.getValue().get("reportCurrency")))
                      .build(), new RecalculatedTradingContext())
              )
          ).collect(Collectors.toMap(
              ImmutablePair::getLeft,
              ImmutablePair::getRight));
    } catch (RuntimeException runtimeException) {
      log.error("Check the format of input parameters, it is not parsable", runtimeException);
      tradingContextMap = Collections.emptyMap();
    }
  }

  private String createReportExchangePair(String reportCurrency) {
    Account<Long> account = accountDataProvider
        .getLatestAccountInfo(bitmexAccountConfiguration.getBitmex()
            .getTradingConfiguration().getAccountId());

    return account.getCurrency() + reportCurrency;
  }

  private void sendTradeConfig(TradingContext tradingContext) {
    if (log.isDebugEnabled()) {
      log.debug("Sending trading context to WS clients {}", tradingContext.toString());
    }

    try {
      simpMessagingTemplate
          .convertAndSend(GeneralConst.WS_TOPIC + GeneralConst.WS_TOPIC_PUBLISH_TRADE_CONFIG,
              new DataResponseMessage<>(
                  modelMapper.map(tradingContext, GridContextResponse.class)));
    } catch (MessagingException messagingException) {
      log.error("Error trading context to websockets", messagingException);
    }
  }

  private void sendTradeCharts(CandleStick candleStick) {
    if (log.isDebugEnabled()) {
      log.debug("Sending chart to WS clients {}", candleStick.toString());
    }

    try {
      simpMessagingTemplate
          .convertAndSend(GeneralConst.WS_TOPIC + GeneralConst.WS_TOPIC_PUBLISH_CHARTS,
              new DataResponseMessage<>(
                  new CandleResponse(
                      candleStick.getInstrument().getInstrument(),
                      candleStick.getOpenPrice(),
                      candleStick.getHighPrice(),
                      candleStick.getLowPrice(),
                      candleStick.getClosePrice(),
                      candleStick.getEventDate().getMillis()
                  )));
    } catch (MessagingException messagingException) {
      log.error("Error sending chart to websockets", messagingException);
    }
  }

  private void sendBitmexLimitResponse(BitmexOrderQuotas bitmexOrderQuotas) {
    if (log.isDebugEnabled()) {
      log.debug("Sending Bitmex order quotas to WS clients {}", bitmexOrderQuotas.toString());
    }
    try {
      simpMessagingTemplate
          .convertAndSend(GeneralConst.WS_TOPIC + GeneralConst.WS_TOPIC_QUOTAS,
              new DataResponseMessage<>(
                  new LimitResponse(
                      bitmexOrderQuotas.getXRatelimitLimit(),
                      bitmexOrderQuotas.getXRatelimitRemaining1s(),
                      bitmexOrderQuotas.getXRatelimitRemaining(),
                      bitmexOrderQuotas.getXRatelimitRemaining() * 1000L
                  )));
    } catch (MessagingException messagingException) {
      log.error("Error sending Bitmex order quotas to websockets", messagingException);
    }
  }

  private void initializeModelMapper() {
    modelMapper.getConfiguration()
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

    Converter<TradeableInstrument, String> locationCodeConverter = context -> context.getSource()
        .getInstrument();

    Converter<CandleStick, Long> dateTimeConverter = context -> context.getSource().getEventDate()
        .getMillis();

    Converter<Map<Integer, TradingDecision<TradingDecisionContext>>, List<MeshEntry>> tradeDecisionMapConverter =
        context -> {
          Map<Integer, TradingDecision<TradingDecisionContext>> source = context.getSource();
          return source != null ? source.values().stream()
              .map(decision -> new MeshEntry(decision.getLimitPrice(),
                  decision.getContext().getLevel()))
              .collect(Collectors.toList()) : Collections.emptyList();
        };

    Converter<Map<Integer, List<BitmexExecution>>, Map<Integer, List<ExecutionResponse>>> executionResponseListConverter =
        context -> {
          Map<Integer, List<BitmexExecution>> source = context.getSource();

          return source != null ? source.entrySet().stream()
              .map(entry -> new ImmutablePair<>(entry.getKey(),
                  entry.getValue().stream().map(bitmexExecution ->
                      ExecutionResponse.builder()
                          .lots(bitmexExecution.getOrderQty())
                          .ordStatus(bitmexExecution.getOrdStatus())
                          .execType(bitmexExecution.getExecType())
                          .ordType(bitmexExecution.getOrdType())
                          .dateTime(bitmexExecution.getTimestamp().getMillis())
                          .lastPx(bitmexExecution.getLastPx())
                          .originalOrderId(bitmexExecution.getOrderID())
                          .price(bitmexExecution.getPrice())
                          .stopPx(bitmexExecution.getStopPx())
                          .side(bitmexExecution.getSide())
                          .build()
                  ).collect(Collectors.toList())
              ))
              .collect(Collectors.toMap(
                  ImmutablePair::getLeft,
                  ImmutablePair::getRight)) :
              Collections.emptyMap();
        };

    modelMapper.addMappings(new PropertyMap<TradingContext, GridContextResponse>() {
      protected void configure() {
        using(locationCodeConverter)
            .map(source.getImmutableTradingContext().getTradeableInstrument())
            .setSymbol(null);
        using(dateTimeConverter)
            .map(source.getRecalculatedTradingContext().getCandleStick())
            .setDateTime(-1L);
        using(tradeDecisionMapConverter)
            .map(source.getRecalculatedTradingContext().getOpenTradingDecisions())
            .setMesh(null);
        using(executionResponseListConverter)
            .map(source.getRecalculatedTradingContext().getExecutionChains())
            .setExecutionResponseList(null);

      }
    });
  }


}
