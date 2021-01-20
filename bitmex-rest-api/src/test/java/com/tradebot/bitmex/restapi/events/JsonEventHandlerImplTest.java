package com.tradebot.bitmex.restapi.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.tradebot.bitmex.restapi.events.handler.JsonEventHandlerImpl;
import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import org.json.simple.JSONObject;
import org.junit.Test;

public class JsonEventHandlerImplTest {

    @Test
    public void generatePayLoad() {
        JsonEventHandlerImpl eventHandler = new JsonEventHandlerImpl();
        // its ok if the we pass null to the constructor here as its not used
        JSONObject jsonPayLoad = mock(JSONObject.class);
        JsonEventPayLoad payLoad = new JsonEventPayLoad(TradeEvents.EVENT_TRADE, jsonPayLoad);
        EmailPayLoad emailPayLoad = eventHandler.generate(payLoad);

        assertThat(emailPayLoad.getBody()).isNotBlank();
        assertThat(emailPayLoad.getSubject()).isNotBlank();
    }


}
