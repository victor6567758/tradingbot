package com.tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradingContext {

    private final ImmutableTradingContext immutableTradingContext;
    private RecalculatedTradingContext recalculatedTradingContext;
}
