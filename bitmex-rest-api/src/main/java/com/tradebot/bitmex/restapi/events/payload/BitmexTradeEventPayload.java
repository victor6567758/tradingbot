package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.bitmex.restapi.model.BitmexTrade;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;

public class BitmexTradeEventPayload extends EventPayLoad<BitmexTrade> implements PayloadAcceptor {

    public BitmexTradeEventPayload(Event event, BitmexTrade payLoad) {
        super(event, payLoad);
    }

    @Override
    public void accept(ProcessedEventVisitor visitor) {
        visitor.visit(this);
    }
}
