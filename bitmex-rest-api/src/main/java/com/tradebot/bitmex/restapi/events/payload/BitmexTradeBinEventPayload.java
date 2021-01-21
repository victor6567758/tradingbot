package com.tradebot.bitmex.restapi.events.payload;

import com.tradebot.bitmex.restapi.model.BitmexTradeBin;
import com.tradebot.core.events.Event;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class BitmexTradeBinEventPayload extends EventPayLoad<BitmexTradeBin> implements PayloadAcceptor {

    private CandleStickGranularity granularity;

    public BitmexTradeBinEventPayload(Event event, BitmexTradeBin payLoad, CandleStickGranularity granularity) {
        super(event, payLoad);
        this.granularity = granularity;
    }

    @Override
    public void accept(ProcessedEventVisitor visitor) {
        visitor.visit(this);
    }
}
