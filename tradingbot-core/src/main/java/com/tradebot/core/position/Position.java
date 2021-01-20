
package com.tradebot.core.position;

import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Position {
    private final TradeableInstrument instrument;
    private final long units;
    private final TradingSignal side;
    private final double averagePrice;
}
