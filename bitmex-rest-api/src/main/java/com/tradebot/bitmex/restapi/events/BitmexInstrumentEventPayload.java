package com.tradebot.bitmex.restapi.events;

import com.tradebot.bitmex.restapi.model.websocket.BitmexInstrument;
import com.tradebot.core.events.EventPayLoad;

public class BitmexInstrumentEventPayload extends EventPayLoad<BitmexInstrument> {

    public BitmexInstrumentEventPayload(TradeEvents event, BitmexInstrument payLoad) {
        super(event, payLoad);
    }
}
