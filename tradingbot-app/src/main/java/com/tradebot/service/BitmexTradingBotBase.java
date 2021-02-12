package com.tradebot.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexInstrumentEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexTradeEventPayload;
import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;
import com.tradebot.bitmex.restapi.events.payload.ProcessedEventVisitor;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.marketdata.BitmexCurrentPriceInfoProvider;
import com.tradebot.bitmex.restapi.marketdata.historic.BitmexHistoricMarketDataProvider;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexInstrument;
import com.tradebot.bitmex.restapi.model.BitmexOrder;
import com.tradebot.bitmex.restapi.model.BitmexTrade;
import com.tradebot.bitmex.restapi.model.BitmexTradeBin;
import com.tradebot.bitmex.restapi.streaming.events.BitmexEventsStreamingService;
import com.tradebot.bitmex.restapi.streaming.marketdata.BitmexMarketDataStreamingService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.events.EventCallbackImpl;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatCallbackImpl;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.InstrumentPairInterestRate;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.marketdata.MarketEventHandlerImpl;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.core.marketdata.historic.HistoricMarketDataProvider;
import com.tradebot.core.streaming.events.EventsStreamingService;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import com.tradebot.event.callback.MarketDataPayLoadSinkCallback;
import com.tradebot.event.callback.TradeBinPayloadSinkCallBack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

@Slf4j
public abstract class BitmexTradingBotBase implements MarketDataPayLoadSinkCallback, TradeBinPayloadSinkCallBack, ProcessedEventVisitor {


    private final Map<TradeableInstrument, Cache<DateTime, Price>> instrumentRecentPricesCache = new HashMap<>();

    private final Map<TradeableInstrument, CacheCandlestick> cacheCandlestickMap = new HashMap<>();

    private final EventBus eventBus;


    private MarketDataStreamingService marketDataStreamingService;

    private EventsStreamingService eventStreamingService;

    protected Map<TradeableInstrument, Map<String, ?>> algParameters;

    protected Map<TradeableInstrument, List<CandleStickGranularity>> instruments;

    protected final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    protected final InstrumentDataProvider instrumentDataProvider = new BitmexInstrumentDataProviderService();

    protected final InstrumentService instrumentService = new InstrumentService(instrumentDataProvider);

    protected final CurrentPriceInfoProvider currentPriceInfoProvider = new BitmexCurrentPriceInfoProvider();

    protected final HistoricMarketDataProvider historicMarketDataProvider = new BitmexHistoricMarketDataProvider();

    protected final AccountDataProvider<Long> accountDataProviderService = new BitmexAccountDataProviderService();

    protected final BlockingQueue<TradingDecision> orderQueue = new LinkedBlockingQueue<>();


    public BitmexTradingBotBase(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void initialize() {

        MarketEventCallback marketEventCallback = new MarketEventHandlerImpl(eventBus);

        HeartBeatCallback<Long> heartBeatCallback = new HeartBeatCallbackImpl<>(eventBus);

        EventCallback<BitmexInstrument> eventBitmexCallback = new EventCallbackImpl<>(eventBus);

        EventCallback<JSONObject> eventJsonObjectCallback = new EventCallbackImpl<>(eventBus);

        EventCallback<BitmexExecution> executionEventCallback = new EventCallbackImpl<>(eventBus);

        EventCallback<BitmexOrder> orderEventCallback = new EventCallbackImpl<>(eventBus);

        EventCallback<BitmexTrade> tradeEventCallback = new EventCallbackImpl<>(eventBus);

        EventCallback<BitmexTradeBin> tradeBinEventCallback = new EventCallbackImpl<>(eventBus);

        algParameters = resolveAlgParameters();
        instruments = resolveInstrumentList();

        initInternalCaches();

        marketDataStreamingService = new BitmexMarketDataStreamingService(
            marketEventCallback, eventBitmexCallback, heartBeatCallback,
            instruments.keySet());

        eventStreamingService = new BitmexEventsStreamingService(
            marketEventCallback,
            eventJsonObjectCallback,
            executionEventCallback,
            orderEventCallback,
            tradeEventCallback,
            tradeBinEventCallback,
            heartBeatCallback,
            instruments.keySet());

        marketDataStreamingService.init();
        marketDataStreamingService.startMarketDataStreaming();

        eventStreamingService.init();
        eventStreamingService.startEventsStreaming();

        loadHistory();

    }

    public void deinitialize() {
        marketDataStreamingService.stopMarketDataStreaming();
        eventStreamingService.stopEventsStreaming();
    }

    @Override
    public void onMarketEvent(Price price) {

        Cache<DateTime, Price> cacheResolved = instrumentRecentPricesCache.get(price.getInstrument());
        if (cacheResolved == null) {
            throw new IllegalArgumentException(String.format("Not subscribed symbol %s", price.getInstrument()));
        }
        cacheResolved.put(price.getPricePoint(), price);

        if (log.isTraceEnabled()) {
            log.trace("Tick data added to tick cache {}", price.getInstrument());
        }
        onTradeSolution(price, cacheResolved);
    }

    @Override
    public void onTradeBinCallback(CandleStick candleStick) {
        CacheCandlestick cacheCandlestick = cacheCandlestickMap.get(candleStick.getInstrument());
        if (cacheCandlestick == null) {
            if (log.isTraceEnabled()) {
                log.trace("Cache is not resolved for symbol {}", candleStick.getInstrument());
            }
            return;
        }

        cacheCandlestick.addCandlestick(candleStick);
        onTradeSolution(candleStick, cacheCandlestick);
    }

    @Override
    public void visit(JsonEventPayLoad event) {
        if (log.isDebugEnabled()) {
            log.debug(event.getPayLoad().toString());
        }
    }

    @Override
    public void visit(BitmexTradeEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug(event.getPayLoad().toString());
        }
    }

    @Override
    public void visit(BitmexOrderEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug(event.getPayLoad().toString());
        }
    }

    @Override
    public void visit(BitmexInstrumentEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug(event.getPayLoad().toString());
        }
    }

    @Override
    public void visit(BitmexExecutionEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug(event.getPayLoad().toString());
        }
    }

    public abstract void onTradeSolution(CandleStick candleStick, CacheCandlestick cacheCandlestick);

    public abstract void onTradeSolution(Price price,
        Cache<DateTime, Price> instrumentRecentPricesCache);


    private void initInternalCaches() {
        for (TradeableInstrument instrument : instruments.keySet()) {
            instrumentRecentPricesCache.put(instrument, CacheBuilder.newBuilder()
                .expireAfterWrite(bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getPriceExpiryMinutes(),
                    TimeUnit.MINUTES).build());
        }

        for (Map.Entry<TradeableInstrument, List<CandleStickGranularity>> entry : instruments.entrySet()) {

            CacheCandlestick cacheCandlestick = new CacheCandlestick(entry.getKey(),
                bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getPriceExpiryMinutes(),
                entry.getValue());

            cacheCandlestickMap.put(entry.getKey(), cacheCandlestick);
        }
    }

    private void loadHistory() {
        for (Map.Entry<TradeableInstrument, List<CandleStickGranularity>> instrument : instruments.entrySet()) {
            CacheCandlestick cacheCandlestick = cacheCandlestickMap.get(instrument.getKey());

            for (CandleStickGranularity granularity : instrument.getValue()) {
                List<CandleStick> history = historicMarketDataProvider.getCandleSticks(instrument.getKey(), granularity,
                    DateTime.now().minus(bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getPriceExpiryMinutes()
                        * 60 * 60 * 1000L),
                    DateTime.now());

                cacheCandlestick.addHistory(instrument.getKey(), history);
            }
        }
    }

    private Map<TradeableInstrument, List<CandleStickGranularity>> resolveInstrumentList() {
        return bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getTradeableInstruments()
            .stream()
            .map(entry ->
                new ImmutablePair<>(new TradeableInstrument(
                    (String) entry.get("instrument"),
                    (String) entry.get("instrumentId"),
                    (Double) entry.get("pip"),
                    new InstrumentPairInterestRate(
                        (Double) entry.get("baseCurrencyBidInterestRate"),
                        (Double) entry.get("baseCurrencyAskInterestRate"),
                        (Double) entry.get("quoteCurrencyBidInterestRate"),
                        (Double) entry.get("quoteCurrencyAskInterestRate")
                    ),
                    (String) entry.get("description")
                ),
                    ((List<String>) entry.get("cacheHistory")).stream()
                        .map(CandleStickGranularity::valueOf).collect(Collectors.toList()))
            ).collect(Collectors.toUnmodifiableMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight));
    }

    private Map<TradeableInstrument, Map<String, ?>> resolveAlgParameters() {
        return bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getTradeableInstruments()
            .stream()
            .map(entry ->
                new ImmutablePair<>(new TradeableInstrument(
                    (String) entry.get("instrument"),
                    (String) entry.get("instrumentId"),
                    (Double) entry.get("pip"),
                    new InstrumentPairInterestRate(
                        (Double) entry.get("baseCurrencyBidInterestRate"),
                        (Double) entry.get("baseCurrencyAskInterestRate"),
                        (Double) entry.get("quoteCurrencyBidInterestRate"),
                        (Double) entry.get("quoteCurrencyAskInterestRate")
                    ),
                    (String) entry.get("description")
                ),
                    ((Map<String, String>) entry.get("algParameters")))
            ).collect(Collectors.toUnmodifiableMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight));
    }


}
