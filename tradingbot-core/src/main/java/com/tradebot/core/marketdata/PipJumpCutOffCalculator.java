package com.tradebot.core.marketdata;

import com.tradebot.core.instrument.TradeableInstrument;

public interface PipJumpCutOffCalculator<T> {

    Double calculatePipJumpCutOff(TradeableInstrument instrument);
}
