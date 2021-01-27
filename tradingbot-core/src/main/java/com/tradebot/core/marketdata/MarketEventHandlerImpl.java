
package com.tradebot.core.marketdata;

import com.google.common.eventbus.EventBus;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import org.joda.time.DateTime;

public class MarketEventHandlerImpl implements MarketEventCallback {

    private final EventBus eventBus;

    public MarketEventHandlerImpl(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onMarketEvent(TradeableInstrument instrument, double bid, double ask, DateTime eventDate) {
        eventBus.post(new Price(instrument, bid, ask, eventDate));
    }

    @Override
    public void onTradeBinEvent(
        TradeableInstrument instrument,
        CandleStickGranularity candleStickGranularity,
        DateTime timestamp,
        double open,
        double high,
        double low,
        double close,
        long volume) {

        eventBus.post(new CandleStick(open, high, low, close, timestamp, instrument, candleStickGranularity));
    }

}
