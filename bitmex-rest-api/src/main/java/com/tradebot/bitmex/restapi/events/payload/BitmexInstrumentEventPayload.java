package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.bitmex.restapi.model.BitmexInstrument;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;

public class BitmexInstrumentEventPayload extends EventPayLoad<BitmexInstrument> implements PayloadAcceptor {

    public BitmexInstrumentEventPayload(Event event, BitmexInstrument payLoad) {
        super(event, payLoad);
    }

    @Override
    public void accept(ProcessedEventVisitor visitor) {
        visitor.visit(this);
    }
}
