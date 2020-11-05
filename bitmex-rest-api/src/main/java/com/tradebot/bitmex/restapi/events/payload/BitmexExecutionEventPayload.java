package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.bitmex.restapi.model.websocket.BitmexExecution;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;

public class BitmexExecutionEventPayload extends EventPayLoad<BitmexExecution> {

    public BitmexExecutionEventPayload(Event event, BitmexExecution payLoad) {
        super(event, payLoad);
    }
}
