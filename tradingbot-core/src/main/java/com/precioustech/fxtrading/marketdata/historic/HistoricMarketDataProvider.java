package com.precioustech.fxtrading.marketdata.historic;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import java.util.List;
import org.joda.time.DateTime;

public interface HistoricMarketDataProvider<T> {

    List<CandleStick<T>> getCandleSticks(
        TradeableInstrument<T> instrument, CandleStickGranularity granularity, DateTime from,
        DateTime to);

    List<CandleStick<T>> getCandleSticks(
        TradeableInstrument<T> instrument, CandleStickGranularity granularity, int count);
}
