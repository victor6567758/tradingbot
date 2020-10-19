
package com.tradebot.core.marketdata;

import com.tradebot.core.instrument.TradeableInstrument;
import org.joda.time.DateTime;


public interface MarketEventCallback<T> {

    void onMarketEvent(TradeableInstrument<T> instrument, double bid, double ask,
        DateTime eventDate);
}
