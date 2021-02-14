package com.tradebot.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexInstrumentEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexTradeEventPayload;
import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;
import com.tradebot.bitmex.restapi.events.payload.ProcessedEventVisitor;
import com.tradebot.bitmex.restapi.streaming.events.BitmexEventsStreamingService;
import com.tradebot.bitmex.restapi.streaming.marketdata.BitmexMarketDataStreamingService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.events.EventCallbackImpl;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatCallbackImpl;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.marketdata.MarketEventHandlerImpl;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.core.marketdata.historic.HistoricMarketDataProvider;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.core.streaming.events.EventsStreamingService;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import com.tradebot.event.callback.MarketDataPayLoadSinkCallback;
import com.tradebot.event.callback.TradeBinPayloadSinkCallBack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;

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

    protected final InstrumentService instrumentService;

    //protected final CurrentPriceInfoProvider currentPriceInfoProvider = new BitmexCurrentPriceInfoProvider();

    protected final HistoricMarketDataProvider historicMarketDataProvider;

    protected final AccountDataProvider<Long> accountDataProvider;

    //protected final BlockingQueue<TradingDecision> orderQueue = new LinkedBlockingQueue<>();


    public BitmexTradingBotBase(
        EventBus eventBus,
        InstrumentService instrumentService,
        AccountDataProvider<Long> accountDataProvider,
        HistoricMarketDataProvider historicMarketDataProvider) {
        this.eventBus = eventBus;
        this.instrumentService = instrumentService;
        this.accountDataProvider = accountDataProvider;
        this.historicMarketDataProvider = historicMarketDataProvider;
    }

    public void initialize() {
        MarketEventCallback marketEventCallback = new MarketEventHandlerImpl(eventBus);
        HeartBeatCallback<Long> heartBeatCallback = new HeartBeatCallbackImpl<>(eventBus);

        algParameters = resolveAlgParameters();
        instruments = resolveInstrumentList();

        initInternalCaches();

        marketDataStreamingService = new BitmexMarketDataStreamingService(
            marketEventCallback,
            new EventCallbackImpl<>(eventBus),
            heartBeatCallback,
            instruments.keySet(),
            instrumentService);

        eventStreamingService = new BitmexEventsStreamingService(
            marketEventCallback,
            new EventCallbackImpl<>(eventBus),
            new EventCallbackImpl<>(eventBus),
            new EventCallbackImpl<>(eventBus),
            new EventCallbackImpl<>(eventBus),
            heartBeatCallback,
            instruments.keySet(),
            instrumentService);

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

    public abstract void onOrderResult(OrderResultContext<String> orderResultContext);

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
                new ImmutablePair<>(
                    instrumentService.resolveTradeableInstrument((String) entry.get("instrument")),
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
                new ImmutablePair<>(
                    instrumentService.resolveTradeableInstrument((String) entry.get("instrument")),
                    ((Map<String, String>) entry.get("algParameters")))
            ).collect(Collectors.toUnmodifiableMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight));
    }


}
