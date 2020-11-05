package com.tradebot.bitmex.restapi.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.tradebot.bitmex.restapi.events.handler.TradeEventHandlerImpl;
import com.tradebot.bitmex.restapi.events.payload.TradeEventPayLoad;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import org.json.simple.JSONObject;
import org.junit.Test;

public class TradeEventHandlerImplTest {

    @Test
    public void generatePayLoad() {
        TradeEventHandlerImpl eventHandler = new TradeEventHandlerImpl();
        // its ok if the we pass null to the constructor here as its not used
        JSONObject jsonPayLoad = mock(JSONObject.class);
        TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.EVENT_TRADE, jsonPayLoad);
        EmailPayLoad emailPayLoad = eventHandler.generate(payLoad);

        assertThat(emailPayLoad.getBody()).isNotBlank();
        assertThat(emailPayLoad.getSubject()).isNotBlank();
    }


}
