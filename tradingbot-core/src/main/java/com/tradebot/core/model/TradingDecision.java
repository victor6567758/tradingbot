package com.tradebot.core.model;

import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = {"context"})
@Builder
public class TradingDecision<C> {

    public enum SrcDecision {
        SOCIAL_MEDIA,
        SPIKE,
        FADE_THE_MOVE,
        OTHER
    }

    private final int executionDelay;
    private final TradingSignal signal;
    private final TradeableInstrument instrument;
    private final SrcDecision tradeSource;
    private final double limitPrice;
    private final double stopPrice;
    private final long units;
    private final double takeProfitPrice;
    private final double stopLossPrice;
    private final C context;


}
