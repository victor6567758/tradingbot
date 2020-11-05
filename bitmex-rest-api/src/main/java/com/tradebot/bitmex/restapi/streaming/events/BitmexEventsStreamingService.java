package com.tradebot.bitmex.restapi.streaming.events;

import com.google.common.reflect.TypeToken;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexTradeEventPayload;
import com.tradebot.bitmex.restapi.events.payload.TradeEventPayLoad;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.model.websocket.BitmexExecution;
import com.tradebot.bitmex.restapi.model.websocket.BitmexOrder;
import com.tradebot.bitmex.restapi.model.websocket.BitmexResponse;
import com.tradebot.bitmex.restapi.model.websocket.BitmexTrade;
import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.streaming.events.EventsStreamingService;
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
    private static final String EXECUTION = "execution";

    public BitmexEventsStreamingService(
        EventCallback<JSONObject> eventCallback,
        EventCallback<BitmexExecution> executionEventCallback,
        EventCallback<BitmexOrder> orderEventCallback,
        EventCallback<BitmexTrade> tradeEventCallback,
        HeartBeatCallback<Long> heartBeatCallback) {
        super(heartBeatCallback);
        this.eventCallback = eventCallback;
        this.executionEventCallback = executionEventCallback;
        this.orderEventCallback = orderEventCallback;
        this.tradeEventCallback = tradeEventCallback;

        initMapping(new MappingFunction[]{
            new MappingFunction(this::processAnnouncementReply, ANNOUNCEMENT),
            new MappingFunction(this::processInsuranceReply, INSURANCE),
            new MappingFunction(this::processPublicNotificationsReply, PUBLIC_NOTIFICATIONS),
            new MappingFunction(this::processOrder, ORDER),
            new MappingFunction(this::processTrade, TRADE),
            new MappingFunction(this::processExecution, EXECUTION)
        });
    }

    private final EventCallback<JSONObject> eventCallback;
    private final EventCallback<BitmexExecution> executionEventCallback;
    private final EventCallback<BitmexOrder> orderEventCallback;
    private final EventCallback<BitmexTrade> tradeEventCallback;


    @Override
    protected String extractSubscribeTopic(String subscribeElement) {
        return subscribeElement;
    }

    @Override
    public void startEventsStreaming() {
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(PUBLIC_NOTIFICATIONS));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(ORDER));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(TRADE));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(EXECUTION));
    }

    @Override
    public void stopEventsStreaming() {
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(PUBLIC_NOTIFICATIONS));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(ORDER));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(TRADE));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(EXECUTION));
    }

    private void processAnnouncementReply(String message) {
        eventCallback.onEvent(new TradeEventPayLoad(
            TradeEvents.EVENT_ANNOUNCEMENT, (JSONObject) JSONValue.parse(message)));
    }

    private void processInsuranceReply(String message) {
        eventCallback.onEvent(new TradeEventPayLoad(
            TradeEvents.EVENT_INSURANCE, (JSONObject) JSONValue.parse(message)));
    }

    private void processPublicNotificationsReply(String message) {
        eventCallback.onEvent(new TradeEventPayLoad(
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
            tradeEventCallback.onEvent(new BitmexTradeEventPayload(TradeEvents.EVENT_TRADE, trade));

            if (log.isDebugEnabled()) {
                log.debug("Parsed trade event: {}", trade.toString());
            }
        }
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
}
