package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;
import org.json.simple.JSONObject;


public class JsonEventPayLoad extends EventPayLoad<JSONObject> implements PayloadAcceptor {

    public JsonEventPayLoad(Event event, JSONObject payLoad) {
        super(event, payLoad);
    }

    @Override
    public void accept(ProcessedEventVisitor visitor) {
        visitor.visit(this);
    }

}
