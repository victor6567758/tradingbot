package com.tradebot.bitmex.restapi.streaming.events;

import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService2;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.streaming.events.EventsStreamingService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

@Slf4j
public class BitmexEventsStreamingService2 extends BaseBitmexStreamingService2 implements EventsStreamingService {

    private static final String ANNOUNCEMENT = "announcement";
    private static final String INSURANCE = "insurance";
    private static final String PUBLIC_NOTIFICATIONS = "publicNotifications";

    public BitmexEventsStreamingService2(
        EventCallback<JSONObject> eventCallback,
        HeartBeatCallback<DateTime> heartBeatCallback) {
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
    public void startEventsStreaming() {
        connect();
    }

    @Override
    public void stopEventsStreaming() {
        disconnect();
    }

    @Override
    protected void connect() {
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildSubscribeCommand(PUBLIC_NOTIFICATIONS));
    }

    @Override
    protected void disconnect() {
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(ANNOUNCEMENT));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(INSURANCE));
        jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(PUBLIC_NOTIFICATIONS));
    }

    private void processAnnouncementReply(String message) {
        if (log.isDebugEnabled()) {
            log.debug("parsed announcement event: {}", message);
        }
    }

    private void processInsuranceReply(String message) {
        if (log.isDebugEnabled()) {
            log.debug("parsed insurance event: {}", message);
        }
    }

    private void processPublicNotificationsReply(String message) {
        if (log.isDebugEnabled()) {
            log.debug("parsed publicNotifications event: {}", message);
        }
    }
}
