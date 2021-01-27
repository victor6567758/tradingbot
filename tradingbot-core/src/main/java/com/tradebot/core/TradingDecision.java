package com.tradebot.core;

import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TradingDecision {
    private final TradingSignal signal;
    // private final double bidPriceAtDecision, askPriceAtDecision;
    private final TradeableInstrument instrument;
    private final double takeProfitPrice;
    private final double stopLossPrice;
    private final SrcDecison tradeSource;
    private final double limitPrice;

    public enum SrcDecison {
        /*INTERNAL,*/SOCIAL_MEDIA, SPIKE, FADE_THE_MOVE, OTHER/*, CCY_EVENT*/
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal) {
        this(instrument, signal, SrcDecison.OTHER);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, SrcDecison tradeSource) {
        this(instrument, signal, 0.0, tradeSource);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, double takeProfitPrice) {
        this(instrument, signal, takeProfitPrice, SrcDecison.OTHER);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, double takeProfitPrice,
            SrcDecison tradeSource) {
        this(instrument, signal, takeProfitPrice, 0.0, tradeSource);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, double takeProfitPrice,
            double stopLossPrice) {
        this(instrument, signal, takeProfitPrice, stopLossPrice, SrcDecison.OTHER);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, double takeProfitPrice,
            double stopLossPrice, SrcDecison tradeSource) {
        this(instrument, signal, takeProfitPrice, stopLossPrice, 0.0, tradeSource);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, double takeProfitPrice,
            double stopLossPrice, double limitPrice) {
        this(instrument, signal, takeProfitPrice, stopLossPrice, limitPrice, SrcDecison.OTHER);
    }

    public TradingDecision(TradeableInstrument instrument, TradingSignal signal, double takeProfitPrice,
            double stopLossPrice, double limitPrice, SrcDecison tradeSource) {
        this.signal = signal;
        this.instrument = instrument;
        this.limitPrice = limitPrice;
        this.tradeSource = tradeSource;
        this.takeProfitPrice = takeProfitPrice;
        this.stopLossPrice = stopLossPrice;
    }

}
