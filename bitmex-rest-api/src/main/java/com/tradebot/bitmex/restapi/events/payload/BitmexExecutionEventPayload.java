package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;

public class BitmexExecutionEventPayload extends EventPayLoad<BitmexExecution> implements PayloadAcceptor {

    public BitmexExecutionEventPayload(Event event, BitmexExecution payLoad) {
        super(event, payLoad);
    }

    @Override
    public void accept(ProcessedEventVisitor visitor) {
        visitor.visit(this);
    }
}
