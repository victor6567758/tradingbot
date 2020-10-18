
package com.precioustech.fxtrading.marketdata;

import org.joda.time.DateTime;

import com.google.common.eventbus.EventBus;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

public class MarketEventHandlerImpl<T> implements MarketEventCallback<T> {

    private final EventBus eventBus;

    public MarketEventHandlerImpl(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onMarketEvent(TradeableInstrument<T> instrument, double bid, double ask, DateTime eventDate) {
        MarketDataPayLoad<T> payload = new MarketDataPayLoad<T>(bid, ask, instrument, eventDate);
        eventBus.post(payload);

    }

}
