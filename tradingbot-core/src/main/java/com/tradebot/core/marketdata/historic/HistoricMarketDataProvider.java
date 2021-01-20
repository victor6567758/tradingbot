package com.tradebot.core.marketdata.historic;

import com.tradebot.core.instrument.TradeableInstrument;
import java.util.List;
import org.joda.time.DateTime;

public interface HistoricMarketDataProvider {

    List<CandleStick> getCandleSticks(
        TradeableInstrument instrument,
        CandleStickGranularity granularity,
        DateTime from,
        DateTime to);

    List<CandleStick> getCandleSticks(
        TradeableInstrument instrument,
        CandleStickGranularity granularity,
        int count);
}
