package com.tradebot.bitmex.restapi.events;

import com.tradebot.bitmex.restapi.model.BitmexInstrument;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;

public class BitmexInstrumentEventPayload extends EventPayLoad<BitmexInstrument> {

    public BitmexInstrumentEventPayload(Event event, BitmexInstrument payLoad) {
        super(event, payLoad);
    }
}
