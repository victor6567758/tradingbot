
package com.tradebot.core.marketdata;

import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import org.joda.time.DateTime;


public interface MarketEventCallback {

    void onMarketEvent(TradeableInstrument instrument, double bid, double ask, DateTime eventDate);
    
    void onTradeBinEvent(
        TradeableInstrument instrument,
        CandleStickGranularity candleStickGranularity,
        DateTime timestamp,
        double open,
        double high,
        double low,
        double close,
        long volume);
}
