package com.tradebot.bitmex.restapi.events;

import com.tradebot.core.events.EventPayLoad;
import org.json.simple.JSONObject;


public class TradeEventPayLoad extends EventPayLoad<JSONObject> {

    public TradeEventPayLoad(TradeEvents event, JSONObject payLoad) {
        super(event, payLoad);
    }

}
