package com.tradebot.bitmex.restapi.events.handler;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;
import com.tradebot.core.events.EventHandler;
import com.tradebot.core.events.EventPayLoadToTweet;
import com.tradebot.core.events.notification.email.EmailContentGenerator;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

@NoArgsConstructor
public class JsonEventHandlerImpl implements
    EventHandler<JSONObject, JsonEventPayLoad>,
    EmailContentGenerator<JSONObject, JsonEventPayLoad>,
    EventPayLoadToTweet<JSONObject, JsonEventPayLoad> {

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleEvent(JsonEventPayLoad payLoad) {

    }

    @Override
    public EmailPayLoad generate(JsonEventPayLoad payLoad) {
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        return new EmailPayLoad("Trade event", String.format("Trade event %s received ", jsonPayLoad));
    }

    @Override
    public String toTweet(JsonEventPayLoad payLoad) {
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        return String.format("Trade event %s received ", jsonPayLoad);
    }

}
