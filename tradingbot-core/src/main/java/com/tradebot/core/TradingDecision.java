package com.tradebot.core;

import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = {"context"})
@Builder
public class TradingDecision<C> {

    public enum SrcDecison {
        SOCIAL_MEDIA,
        SPIKE,
        FADE_THE_MOVE,
        OTHER
    }

    private final TradingSignal signal;
    private final TradeableInstrument instrument;
    private final SrcDecison tradeSource;
    private final double limitPrice;
    private final double stopPrice;
    private final long units;
    private final double takeProfitPrice;
    private final double stopLossPrice;
    private final C context;


}
