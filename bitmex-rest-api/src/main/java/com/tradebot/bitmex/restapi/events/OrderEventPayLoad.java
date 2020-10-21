package com.tradebot.bitmex.restapi.events;

import com.tradebot.core.events.EventPayLoad;
import org.json.simple.JSONObject;


public class OrderEventPayLoad extends EventPayLoad<JSONObject> {

    private final OrderEvents orderEvent;

    public OrderEventPayLoad(OrderEvents event, JSONObject payLoad) {
        super(event, payLoad);
        this.orderEvent = event;
    }

    @Override
    public OrderEvents getEvent() {
        return this.orderEvent;
    }

}
