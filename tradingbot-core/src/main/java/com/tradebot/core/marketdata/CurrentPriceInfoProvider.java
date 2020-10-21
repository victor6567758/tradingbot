package com.tradebot.core.marketdata;

import java.util.Collection;
import java.util.Map;

import com.tradebot.core.instrument.TradeableInstrument;

public interface CurrentPriceInfoProvider<T> {

    Map<TradeableInstrument<T>, Price<T>> getCurrentPricesForInstruments(Collection<TradeableInstrument<T>> instruments);

    Price<T> getCurrentPricesForInstrument(TradeableInstrument<T> instrument);
}
