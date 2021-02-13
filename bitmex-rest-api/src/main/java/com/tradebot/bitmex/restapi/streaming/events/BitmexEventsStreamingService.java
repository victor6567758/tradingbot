package com.tradebot.bitmex.restapi.streaming.events;

import com.google.common.reflect.TypeToken;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexTradeEventPayload;
import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexOrder;
import com.tradebot.bitmex.restapi.model.BitmexResponse;
import com.tradebot.bitmex.restapi.model.BitmexTrade;
import com.tradebot.bitmex.restapi.model.BitmexTradeBin;
import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.core.streaming.events.EventsStreamingService;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class BitmexEventsStreamingService extends BaseBitmexStreamingService implements EventsStreamingService {

    private static final String ANNOUNCEMENT = "announcement";
    private static final String INSURANCE = "insurance";
    private static final String PUBLIC_NOTIFICATIONS = "publicNotifications";
    private static final String ORDER = "order";
    private static final String TRADE = "trade";
    private static final String TRADE_BIN1M = "tradeBin1m";
    private static final String TRADE_BIN5M = "tradeBin5m";
    private static final String TRADE_BIN1H = "tradeBin1h";
    private static final String TRADE_BIN1D = "tradeBin1d";
    private static final String EXECUTION = "execution";

    private final InstrumentService instrumentService;

    public BitmexEventsStreamingService(
        MarketEventCallback marketEventCallback,
        EventCallback<JSONObject> eventCallback,
        EventCallback<BitmexExecution> executionEventCallback,
        EventCallback<BitmexOrder> orderEventCallback,
        EventCallback<BitmexTrade> tradeEventCallback,
        EventCallback<BitmexTradeBin> tradeBinEventCallback,
        HeartBeatCallback<Long> heartBeatCallback,
        Collection<TradeableInstrument> instruments,
        InstrumentService instrumentService) {

        super(heartBeatCallback);
        this.marketEventCallback = marketEventCallback;
        this.eventCallback = eventCallback;
        this.executionEventCallback = executionEventCallback;
        this.orderEventCallback = orderEventCallback;
        this.tradeEventCallback = tradeEventCallback;
        this.tradeBinEventCallback = tradeBinEventCallback;
        this.instruments = instruments;
        this.instrumentService = instrumentService;

        validRawInstruments =
            instruments.stream().map(TradeableInstrument::getInstrument).collect(Collectors.toUnmodifiableSet());

        initMapping(new MappingFunction[]{
            new MappingFunction(this::processAnnouncementReply, ANNOUNCEMENT),
            new MappingFunction(this::processInsuranceReply, INSURANCE),
            new MappingFunction(this::processPublicNotificationsReply, PUBLIC_NOTIFICATIONS),
            new MappingFunction(this::processOrder, ORDER),
            new MappingFunction(this::processTrade, TRADE),

            new MappingFunction(this::processTradeBin1M, TRADE_BIN1M),
            new MappingFunction(this::processTradeBin5M, TRADE_BIN5M),
            new MappingFunction(this::processTradeBin1H, TRADE_BIN1H),
            new MappingFunction(this::processTradeBin1D, TRADE_BIN1D),

            new MappingFunction(this::processExecution, EXECUTION)
        });
    }

    private final MarketEventCallback marketEventCallback;
    private final EventCallback<JSONObject> eventCallback;
    private final EventCallback<BitmexExecution> executionEventCallback;
    private final EventCallback<BitmexOrder> orderEventCallback;
    private final EventCallback<BitmexTrade> tradeEventCallback;
    private final EventCallback<BitmexTradeBin> tradeBinEventCallback;
    private final Collection<TradeableInstrument> instruments;
    private final Set<String> validRawInstruments;


    @Override
    protected String extractSubscribeTopic(String subscribeElement) {
        return subscribeElement;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public void startEventsStreaming() {
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(PUBLIC_NOTIFICATIONS));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(ORDER));

        jettyCommunicationSocket.subscribe(buildSubscribeCommand(TRADE));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(TRADE_BIN1M));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(TRADE_BIN5M));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(TRADE_BIN1H));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(TRADE_BIN1D));

        jettyCommunicationSocket.subscribe(buildSubscribeCommand(EXECUTION));
    }

    @Override
    public void stopEventsStreaming() {
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(PUBLIC_NOTIFICATIONS));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(ORDER));

        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(TRADE));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(TRADE_BIN1M));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(TRADE_BIN5M));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(TRADE_BIN1H));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(TRADE_BIN1D));

        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(EXECUTION));
    }

    private void processAnnouncementReply(String message) {
        eventCallback.onEvent(new JsonEventPayLoad(
            TradeEvents.EVENT_ANNOUNCEMENT, (JSONObject) JSONValue.parse(message)));
    }

    private void processInsuranceReply(String message) {
        eventCallback.onEvent(new JsonEventPayLoad(
            TradeEvents.EVENT_INSURANCE, (JSONObject) JSONValue.parse(message)));
    }

    private void processPublicNotificationsReply(String message) {
        eventCallback.onEvent(new JsonEventPayLoad(
            TradeEvents.EVENT_PUBLIC_NOTIFICATION, (JSONObject) JSONValue.parse(message)));
    }

    private void processOrder(String message) {
        BitmexResponse<BitmexOrder> orders = parseMessage(message, new TypeToken<>() {
        });

        for (BitmexOrder order : orders.getData()) {
            orderEventCallback.onEvent(new BitmexOrderEventPayload(TradeEvents.EVENT_ORDER, order));

            if (log.isDebugEnabled()) {
                log.debug("Parsed order event: {}", order.toString());
            }
        }
    }

    private void processTrade(String message) {
        BitmexResponse<BitmexTrade> trades = parseMessage(message, new TypeToken<>() {
        });

        for (BitmexTrade trade : trades.getData()) {
            if (validRawInstruments.contains(trade.getSymbol())) {
                tradeEventCallback.onEvent(new BitmexTradeEventPayload(TradeEvents.EVENT_TRADE, trade));

                if (log.isDebugEnabled()) {
                    log.debug("Parsed trade event: {}", trade.toString());
                }
            }
        }
    }

    private void processTradeBin1M(String message) {

        propagateTradeBin(message, CandleStickGranularity.M1);
    }

    private void processTradeBin5M(String message) {
        propagateTradeBin(message, CandleStickGranularity.M5);
    }

    private void processTradeBin1H(String message) {
        propagateTradeBin(message, CandleStickGranularity.H1);
    }

    private void processTradeBin1D(String message) {
        propagateTradeBin(message, CandleStickGranularity.D);
    }

    private void processExecution(String message) {
        BitmexResponse<BitmexExecution> executions = parseMessage(message, new TypeToken<>() {
        });

        for (BitmexExecution execution : executions.getData()) {
            executionEventCallback.onEvent(new BitmexExecutionEventPayload(TradeEvents.EVENT_EXECUTION, execution));

            if (log.isDebugEnabled()) {
                log.debug("Parsed execution event: {}", execution.toString());
            }
        }
    }

    private void propagateTradeBin(String message, CandleStickGranularity granularity) {
        BitmexResponse<BitmexTradeBin> tradeBins = parseMessage(message, new TypeToken<>() {
        });

        for (BitmexTradeBin tradeBin : tradeBins.getData()) {
            if (validRawInstruments.contains(tradeBin.getSymbol())) {

                marketEventCallback.onTradeBinEvent(
                    instrumentService.resolveTradeableInstrument(tradeBin.getSymbol()),
                    granularity,
                    tradeBin.getTimestamp(),
                    tradeBin.getOpen(),
                    tradeBin.getHigh(),
                    tradeBin.getLow(),
                    tradeBin.getClose(),
                    tradeBin.getVolume()
                );
                if (log.isDebugEnabled()) {
                    log.debug("Parsed trade bin: {}", tradeBin.toString());
                }
            }
        }
    }
}
