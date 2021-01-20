package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.bitmex.restapi.model.BitmexOrder;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;

public class BitmexOrderEventPayload extends EventPayLoad<BitmexOrder> implements PayloadAcceptor {

    public BitmexOrderEventPayload(Event event, BitmexOrder payLoad) {
        super(event, payLoad);
    }

    @Override
    public void accept(ProcessedEventVisitor visitor) {
        visitor.visit(this);
    }
}
