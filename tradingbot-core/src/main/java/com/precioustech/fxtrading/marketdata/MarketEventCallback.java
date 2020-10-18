
package com.precioustech.fxtrading.marketdata;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import org.joda.time.DateTime;


public interface MarketEventCallback<T> {

    void onMarketEvent(TradeableInstrument<T> instrument, double bid, double ask,
        DateTime eventDate);
}
