package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;
import org.json.simple.JSONObject;


public class TradeEventPayLoad extends EventPayLoad<JSONObject> {

    public TradeEventPayLoad(Event event, JSONObject payLoad) {
        super(event, payLoad);
    }

}
