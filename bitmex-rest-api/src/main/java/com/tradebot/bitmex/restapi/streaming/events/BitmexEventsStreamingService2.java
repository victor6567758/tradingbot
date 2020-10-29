package com.tradebot.bitmex.restapi.streaming.events;

import com.tradebot.bitmex.restapi.events.TradeEventPayLoad;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService2;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.streaming.events.EventsStreamingService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class BitmexEventsStreamingService2 extends BaseBitmexStreamingService2 implements EventsStreamingService {

    private static final String ANNOUNCEMENT = "announcement";
    private static final String INSURANCE = "insurance";
    private static final String PUBLIC_NOTIFICATIONS = "publicNotifications";

    public BitmexEventsStreamingService2(
        EventCallback<JSONObject> eventCallback,
        HeartBeatCallback<Long> heartBeatCallback) {
        super(heartBeatCallback);
        this.eventCallback = eventCallback;

        initMapping(new MappingFunction[]{
            new MappingFunction(this::processAnnouncementReply, ANNOUNCEMENT),
            new MappingFunction(this::processInsuranceReply, INSURANCE),
            new MappingFunction(this::processPublicNotificationsReply, PUBLIC_NOTIFICATIONS)
        });
    }

    private final EventCallback<JSONObject> eventCallback;


    @Override
    protected String extractSubscribeTopic(String subscribeElement) {
        return subscribeElement;
    }

    @Override
    public void startEventsStreaming() {
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(PUBLIC_NOTIFICATIONS));
    }

    @Override
    public void stopEventsStreaming() {
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(PUBLIC_NOTIFICATIONS));
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
}
