
package com.precioustech.fxtrading.position;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Position<T> {
    private final TradeableInstrument<T> instrument;
    private final long units;
    private final TradingSignal side;
    private final double averagePrice;
}
