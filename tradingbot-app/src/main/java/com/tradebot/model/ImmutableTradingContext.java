package com.tradebot.model;

import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ImmutableTradingContext {
    private final double xPct;
    private final double priceEnd;
    private final int linesNum;
    private final int orderPosUnits;
    private final TradeableInstrument tradeableInstrument;
    private final String reportCurrency;
    private final String reportExchangePair;
}
