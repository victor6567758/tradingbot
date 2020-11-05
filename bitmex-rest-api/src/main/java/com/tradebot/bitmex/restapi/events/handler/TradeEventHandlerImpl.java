package com.tradebot.bitmex.restapi.events.handler;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.events.payload.TradeEventPayLoad;
import com.tradebot.core.events.EventHandler;
import com.tradebot.core.events.EventPayLoadToTweet;
import com.tradebot.core.events.notification.email.EmailContentGenerator;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

@NoArgsConstructor
public class TradeEventHandlerImpl implements
    EventHandler<JSONObject, TradeEventPayLoad>,
    EmailContentGenerator<JSONObject, TradeEventPayLoad>,
    EventPayLoadToTweet<JSONObject, TradeEventPayLoad> {

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleEvent(TradeEventPayLoad payLoad) {

    }

    @Override
    public EmailPayLoad generate(TradeEventPayLoad payLoad) {
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        return new EmailPayLoad("Trade event", String.format("Trade event %s received ", jsonPayLoad));
    }

    @Override
    public String toTweet(TradeEventPayLoad payLoad) {
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        return String.format("Trade event %s received ", jsonPayLoad);
    }

}
