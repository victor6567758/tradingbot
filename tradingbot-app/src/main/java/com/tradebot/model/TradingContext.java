package com.tradebot.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradingContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ImmutableTradingContext immutableTradingContext;
    private RecalculatedTradingContext recalculatedTradingContext;
}
