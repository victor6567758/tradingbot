package com.tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
public class TradingContext {

    private final ImmutableTradingContext immutableTradingContext;
    private RecalculatedTradingContext recalculatedTradingContext;
}
