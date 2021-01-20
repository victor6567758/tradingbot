
package com.tradebot.core.marketdata.historic;

import com.tradebot.core.instrument.TradeableInstrument;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;

public class MovingAverageCalculationService {

    private final HistoricMarketDataProvider historicMarketDataProvider;

    public MovingAverageCalculationService(
        HistoricMarketDataProvider historicMarketDataProvider) {
        this.historicMarketDataProvider = historicMarketDataProvider;
    }

    public double calculateSMA(TradeableInstrument instrument, int count,
        CandleStickGranularity granularity) {
        List<CandleStick> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, count);
        return calculateSMA(candles);
    }

    public double calculateSMA(TradeableInstrument instrument, DateTime from, DateTime to,
        CandleStickGranularity granularity) {
        List<CandleStick> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, from, to);
        return calculateSMA(candles);
    }


    private double calculateSMA(List<CandleStick> candles) {
        double sumsma = 0;
        for (CandleStick candle : candles) {
            sumsma += candle.getClosePrice();
        }
        return sumsma / candles.size();
    }

    public ImmutablePair<Double, Double> calculateSMAandWMAasPair(TradeableInstrument instrument,
        int count,
        CandleStickGranularity granularity) {
        List<CandleStick> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, count);
        return new ImmutablePair<>(calculateSMA(candles), calculateWMA(candles));
    }

    public ImmutablePair<Double, Double> calculateSMAandWMAasPair(TradeableInstrument instrument,
        DateTime from,
        DateTime to, CandleStickGranularity granularity) {
        List<CandleStick> candles =
            historicMarketDataProvider.getCandleSticks(instrument, granularity, from, to);
        return new ImmutablePair<>(calculateSMA(candles), calculateWMA(candles));
    }


    private double calculateWMA(List<CandleStick> candles) {
        double divisor = (candles.size() * (candles.size() + 1)) / 2.0;
        int count = 0;
        double sumwma = 0;
        for (CandleStick candle : candles) {
            count++;
            sumwma += (count * candle.getClosePrice()) / divisor;
        }
        return sumwma;
    }

    public double calculateWMA(TradeableInstrument instrument, int count,
        CandleStickGranularity granularity) {
        List<CandleStick> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, count);
        return calculateWMA(candles);
    }

    public double calculateWMA(TradeableInstrument instrument, DateTime from, DateTime to,
        CandleStickGranularity granularity) {
        List<CandleStick> candles = historicMarketDataProvider
            .getCandleSticks(instrument, granularity, from, to);
        return calculateWMA(candles);
    }
}
