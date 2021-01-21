package com.tradebot.bitmex.restapi.streaming.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.io.Resources;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexOrder;
import com.tradebot.bitmex.restapi.model.BitmexTrade;
import com.tradebot.bitmex.restapi.model.BitmexTradeBin;
import com.tradebot.bitmex.restapi.streaming.JettyCommunicationSocket;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatPayLoad;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class BitmexEventsStreamingServiceTest {

    private static final Collection<TradeableInstrument> INSTRUMENTS = Arrays.asList(
        new TradeableInstrument("XBTUSD", "XBTUSD"),
        new TradeableInstrument("XBTJPY","XBTJPY")
    );

    private JettyCommunicationSocket jettyCommunicationSocketSpy;
    private BitmexEventsStreamingService bitmexEventsStreamingServiceSpy;

    private String publicNotification;
    private String publicAnnouncement;
    private String insurance;

    private JSONObject publicNotificationJson;
    private JSONObject publicAnnouncementJson;
    private JSONObject insuranceJson;

    private String publicNotificationSubscribe;
    private String publicAnnouncementSubscribe;
    private String insuranceSubscribe;

    private HeartBeatCallback<Long> heartBeatCallbackSpy;
    private EventCallback<JSONObject> eventCallbackSpy;

    private EventCallback<BitmexExecution> executionEventCallbackSpy;
    private EventCallback<BitmexOrder> orderEventCallbackSpy;
    private EventCallback<BitmexTrade> tradeEventCallbackSpy;
    private EventCallback<BitmexTradeBin> tradeEventBinCallbackSpy;


    // Must not be lambdas for correct Mockito work
    private final HeartBeatCallback<Long> heartBeatCallback = new HeartBeatCallback<Long>() {

        @Override
        public void onHeartBeat(HeartBeatPayLoad<Long> payLoad) {
        }
    };
    private final EventCallback<JSONObject> eventCallback = new EventCallback<JSONObject>() {

        @Override
        public void onEvent(EventPayLoad<JSONObject> eventPayLoad) {
        }
    };
    private final EventCallback<BitmexExecution> executionEventCallback = new EventCallback<BitmexExecution>() {


        @Override
        public void onEvent(EventPayLoad<BitmexExecution> eventPayLoad) {

        }
    };
    private final EventCallback<BitmexTrade> tradeEventCallback = new EventCallback<BitmexTrade>() {


        @Override
        public void onEvent(EventPayLoad<BitmexTrade> eventPayLoad) {

        }
    };
    private final EventCallback<BitmexTradeBin> tradeEventBinCallback = new EventCallback<BitmexTradeBin>() {


        @Override
        public void onEvent(EventPayLoad<BitmexTradeBin> eventPayLoad) {

        }
    };
    private final EventCallback<BitmexOrder> orderEventCallback = new EventCallback<BitmexOrder>() {


        @Override
        public void onEvent(EventPayLoad<BitmexOrder> eventPayLoad) {

        }
    };

    @Before
    public void init() throws IOException {

        publicNotification = Resources.toString(Resources.getResource("publicNotificationsEvent.json"), StandardCharsets.UTF_8);
        publicAnnouncement = Resources.toString(Resources.getResource("announcementEvent.json"), StandardCharsets.UTF_8);
        insurance = Resources.toString(Resources.getResource("insuranceEvent.json"), StandardCharsets.UTF_8);

        publicNotificationJson = (JSONObject) JSONValue.parse(publicNotification);
        publicAnnouncementJson = (JSONObject) JSONValue.parse(publicAnnouncement);
        insuranceJson = (JSONObject) JSONValue.parse(insurance);

        assertThat(publicNotification).isNotBlank();
        assertThat(publicAnnouncement).isNotBlank();
        assertThat(insurance).isNotBlank();

        publicNotificationSubscribe = Resources.toString(Resources.getResource("subscribePublicNotificationsEvent.json"), StandardCharsets.UTF_8);
        publicAnnouncementSubscribe = Resources.toString(Resources.getResource("subscribeAnnouncementEvent.json"), StandardCharsets.UTF_8);
        insuranceSubscribe = Resources.toString(Resources.getResource("subscribeInsuranceEvent.json"), StandardCharsets.UTF_8);

        assertThat(publicNotificationSubscribe).isNotBlank();
        assertThat(publicAnnouncementSubscribe).isNotBlank();
        assertThat(insuranceSubscribe).isNotBlank();

        heartBeatCallbackSpy = spy(heartBeatCallback);
        eventCallbackSpy = spy(eventCallback);
        executionEventCallbackSpy = spy(executionEventCallback);
        orderEventCallbackSpy = spy(orderEventCallback);
        tradeEventCallbackSpy = spy(tradeEventCallback);
        tradeEventBinCallbackSpy = spy(tradeEventBinCallback);

        bitmexEventsStreamingServiceSpy = spy(new BitmexEventsStreamingService(
            eventCallbackSpy,
            executionEventCallbackSpy,
            orderEventCallbackSpy,
            tradeEventCallbackSpy,
            tradeEventBinCallbackSpy,
            heartBeatCallbackSpy,
            INSTRUMENTS
            )
        );
        jettyCommunicationSocketSpy = spy(bitmexEventsStreamingServiceSpy.getJettyCommunicationSocket());

        doNothing().when(bitmexEventsStreamingServiceSpy).shutdown();
        doNothing().when(bitmexEventsStreamingServiceSpy).init();
        doNothing().when(bitmexEventsStreamingServiceSpy).startEventsStreaming();
        doNothing().when(bitmexEventsStreamingServiceSpy).stopEventsStreaming();

        bitmexEventsStreamingServiceSpy.init();
        bitmexEventsStreamingServiceSpy.startEventsStreaming();
    }

    @After
    public void tearDowmn() {
        bitmexEventsStreamingServiceSpy.stopEventsStreaming();
        bitmexEventsStreamingServiceSpy.shutdown();
    }

    @Test
    public void testAnnouncementEvent() {
        ArgumentCaptor<EventPayLoad<JSONObject>> argumentCaptor = ArgumentCaptor.forClass(EventPayLoad.class);

        jettyCommunicationSocketSpy.onMessage(publicAnnouncementSubscribe);
        jettyCommunicationSocketSpy.onMessage(publicAnnouncement);
        verify(eventCallbackSpy).onEvent(argumentCaptor.capture());

        EventPayLoad<JSONObject> capturedArgument = argumentCaptor.getValue();
        assertThat(capturedArgument.getEvent()).isEqualTo(TradeEvents.EVENT_ANNOUNCEMENT);
        assertThat(capturedArgument.getPayLoad()).isEqualTo(publicAnnouncementJson);
    }

    @Test
    public void testPublicNotificationEvent() {
        ArgumentCaptor<EventPayLoad<JSONObject>> argumentCaptor = ArgumentCaptor.forClass(EventPayLoad.class);

        jettyCommunicationSocketSpy.onMessage(publicNotificationSubscribe);
        jettyCommunicationSocketSpy.onMessage(publicNotification);
        verify(eventCallbackSpy).onEvent(argumentCaptor.capture());

        EventPayLoad<JSONObject> capturedArgument = argumentCaptor.getValue();
        assertThat(capturedArgument.getEvent()).isEqualTo(TradeEvents.EVENT_PUBLIC_NOTIFICATION);
        assertThat(capturedArgument.getPayLoad()).isEqualTo(publicNotificationJson);

    }

    @Test
    public void testInsuranceNotificationEvent() {
        ArgumentCaptor<EventPayLoad<JSONObject>> argumentCaptor = ArgumentCaptor.forClass(EventPayLoad.class);

        jettyCommunicationSocketSpy.onMessage(insuranceSubscribe);
        jettyCommunicationSocketSpy.onMessage(insurance);
        verify(eventCallbackSpy).onEvent(argumentCaptor.capture());

        EventPayLoad<JSONObject> capturedArgument = argumentCaptor.getValue();
        assertThat(capturedArgument.getEvent()).isEqualTo(TradeEvents.EVENT_INSURANCE);
        assertThat(capturedArgument.getPayLoad()).isEqualTo(insuranceJson);
    }

    @Test
    public void testHeartBeat() {
        jettyCommunicationSocketSpy.onMessage("pong");
        verify(heartBeatCallbackSpy, times(1)).onHeartBeat(any(HeartBeatPayLoad.class));
    }


}