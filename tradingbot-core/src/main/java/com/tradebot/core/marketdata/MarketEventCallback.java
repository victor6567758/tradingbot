
package com.tradebot.core.marketdata;

import com.tradebot.core.instrument.TradeableInstrument;
import org.joda.time.DateTime;


public interface MarketEventCallback {

    void onMarketEvent(TradeableInstrument instrument, double bid, double ask,
        DateTime eventDate);
}
