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
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.marketdata.BitmexCurrentPriceInfoProvider;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexInstrument;
import com.tradebot.bitmex.restapi.model.BitmexOrder;
import com.tradebot.bitmex.restapi.model.BitmexTrade;
import com.tradebot.bitmex.restapi.streaming.events.BitmexEventsStreamingService;
import com.tradebot.bitmex.restapi.streaming.marketdata.BitmexMarketDataStreamingService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.events.EventCallbackImpl;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatCallbackImpl;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.InstrumentPairInterestRate;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.MarketDataPayLoad;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.marketdata.MarketEventHandlerImpl;
import com.tradebot.core.streaming.events.EventsStreamingService;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import com.tradebot.service.event.EventPayLoadSink;
import com.tradebot.service.event.MarketDataPayLoadSink;
import com.tradebot.service.event.callback.EventPayLoadCallback;
import com.tradebot.service.event.callback.MarketDataPayLoadSinkCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BitmexTradingBot implements MarketDataPayLoadSinkCallback, ProcessedEventVisitor {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();


    private final InstrumentDataProvider instrumentDataProvider = new BitmexInstrumentDataProviderService();

    private final InstrumentService instrumentService = new InstrumentService(instrumentDataProvider);

    private final CurrentPriceInfoProvider currentPriceInfoProvider = new BitmexCurrentPriceInfoProvider();

    private final BlockingQueue<TradingDecision> orderQueue = new LinkedBlockingQueue<>();

    private final Map<TradeableInstrument, Cache<DateTime, MarketDataPayLoad>> instrumentRecentPricesCache = new HashMap<>();


    private final EventBus eventBus;

    private MarketEventCallback marketEventCallback;

    private HeartBeatCallback<Long> heartBeatCallback;

    private EventCallback<BitmexInstrument> eventBitmexCallback;

    private EventCallback<JSONObject> eventJsonObjectCallback;

    private EventCallback<BitmexExecution> executionEventCallback;

    private EventCallback<BitmexOrder> orderEventCallback;

    private EventCallback<BitmexTrade> tradeEventCallback;

    private MarketDataStreamingService marketDataStreamingService;

    private EventsStreamingService eventStreamingService;


    @PostConstruct
    public void init() {

        marketEventCallback = new MarketEventHandlerImpl(eventBus);

        heartBeatCallback = new HeartBeatCallbackImpl<>(eventBus);

        eventBitmexCallback = new EventCallbackImpl<>(eventBus);

        eventJsonObjectCallback = new EventCallbackImpl<>(eventBus);

        executionEventCallback = new EventCallbackImpl<>(eventBus);

        orderEventCallback = new EventCallbackImpl<>(eventBus);

        tradeEventCallback = new EventCallbackImpl<>(eventBus);

        List<TradeableInstrument> instrumentList = resolveInstrumentList();

        for (TradeableInstrument instrument : instrumentList) {
            Cache<DateTime, MarketDataPayLoad> recentPricesCache = CacheBuilder.newBuilder()
                .expireAfterWrite(bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getPriceExpiryMinutes(),
                    TimeUnit.MINUTES).build();
            instrumentRecentPricesCache.put(instrument, recentPricesCache);
        }

        marketDataStreamingService = new BitmexMarketDataStreamingService(
            marketEventCallback, eventBitmexCallback, heartBeatCallback,
            instrumentList);

        eventStreamingService = new BitmexEventsStreamingService(
            eventJsonObjectCallback, executionEventCallback, orderEventCallback, tradeEventCallback, heartBeatCallback);

        marketDataStreamingService.init();
        marketDataStreamingService.startMarketDataStreaming();

        eventStreamingService.init();
        eventStreamingService.startEventsStreaming();

    }

    @PreDestroy
    public void deInit() {
        marketDataStreamingService.stopMarketDataStreaming();
        eventStreamingService.stopEventsStreaming();
    }


    private List<TradeableInstrument> resolveInstrumentList() {
        return bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getTradeableInstruments()
            .stream()
            .map( entry -> new TradeableInstrument(
                (String)entry.get("instrument"),
                (String)entry.get("instrumentId"),
                (Double)entry.get("pip"),
                new InstrumentPairInterestRate(
                    (Double)entry.get("baseCurrencyBidInterestRate"),
                    (Double)entry.get("baseCurrencyAskInterestRate"),
                    (Double)entry.get("quoteCurrencyBidInterestRate"),
                    (Double)entry.get("quoteCurrencyAskInterestRate")
                ),
                (String)entry.get("description")
            )).collect(Collectors.toList());
    }

    @Override
    public void onMarketEvent(MarketDataPayLoad marketDataPayLoad) {
        if (instrumentRecentPricesCache.containsKey(marketDataPayLoad.getInstrument())) {
            instrumentRecentPricesCache.get(marketDataPayLoad.getInstrument()).put(marketDataPayLoad.getEventDate(),
                marketDataPayLoad);
        }
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
}
