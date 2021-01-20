
package com.tradebot.core.marketdata;

import org.joda.time.DateTime;

import com.google.common.eventbus.EventBus;
import com.tradebot.core.instrument.TradeableInstrument;

public class MarketEventHandlerImpl implements MarketEventCallback {

    private final EventBus eventBus;

    public MarketEventHandlerImpl(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onMarketEvent(TradeableInstrument instrument, double bid, double ask, DateTime eventDate) {
        MarketDataPayLoad payload = new MarketDataPayLoad(bid, ask, instrument, eventDate);
        eventBus.post(payload);
    }

}
