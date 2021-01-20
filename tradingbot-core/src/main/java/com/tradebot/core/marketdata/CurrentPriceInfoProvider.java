package com.tradebot.core.marketdata;

import java.util.Collection;
import java.util.Map;

import com.tradebot.core.instrument.TradeableInstrument;

public interface CurrentPriceInfoProvider {

    Map<TradeableInstrument, Price> getCurrentPricesForInstruments(Collection<TradeableInstrument> instruments);

    Price getCurrentPricesForInstrument(TradeableInstrument instrument);
}
