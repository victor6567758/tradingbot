
package com.precioustech.fxtrading.marketdata.historic;

import com.precioustech.fxtrading.instrument.TradeableInstrument;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;

public class MovingAverageCalculationService<T> {

    private final HistoricMarketDataProvider<T> historicMarketDataProvider;

    public MovingAverageCalculationService(
        HistoricMarketDataProvider<T> historicMarketDataProvider) {
        this.historicMarketDataProvider = historicMarketDataProvider;
    }

    public double calculateSMA(TradeableInstrument<T> instrument, int count,
        CandleStickGranularity granularity) {
        List<CandleStick<T>> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, count);
        return calculateSMA(candles);
    }

    public double calculateSMA(TradeableInstrument<T> instrument, DateTime from, DateTime to,
        CandleStickGranularity granularity) {
        List<CandleStick<T>> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, from, to);
        return calculateSMA(candles);
    }


    private double calculateSMA(List<CandleStick<T>> candles) {
        double sumsma = 0;
        for (CandleStick<T> candle : candles) {
            sumsma += candle.getClosePrice();
        }
        return sumsma / candles.size();
    }

    public ImmutablePair<Double, Double> calculateSMAandWMAasPair(TradeableInstrument<T> instrument,
        int count,
        CandleStickGranularity granularity) {
        List<CandleStick<T>> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, count);
        return new ImmutablePair<>(calculateSMA(candles), calculateWMA(candles));
    }

    public ImmutablePair<Double, Double> calculateSMAandWMAasPair(TradeableInstrument<T> instrument,
        DateTime from,
        DateTime to, CandleStickGranularity granularity) {
        List<CandleStick<T>> candles =
            historicMarketDataProvider.getCandleSticks(instrument, granularity, from, to);
        return new ImmutablePair<>(calculateSMA(candles), calculateWMA(candles));
    }


    private double calculateWMA(List<CandleStick<T>> candles) {
        double divisor = (candles.size() * (candles.size() + 1)) / 2.0;
        int count = 0;
        double sumwma = 0;
        for (CandleStick<T> candle : candles) {
            count++;
            sumwma += (count * candle.getClosePrice()) / divisor;
        }
        return sumwma;
    }

    public double calculateWMA(TradeableInstrument<T> instrument, int count,
        CandleStickGranularity granularity) {
        List<CandleStick<T>> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, count);
        return calculateWMA(candles);
    }

    public double calculateWMA(TradeableInstrument<T> instrument, DateTime from, DateTime to,
        CandleStickGranularity granularity) {
        List<CandleStick<T>> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, from, to);
        return calculateWMA(candles);
    }
}
