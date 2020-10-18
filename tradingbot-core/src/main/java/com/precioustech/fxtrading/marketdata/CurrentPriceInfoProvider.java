package com.precioustech.fxtrading.marketdata;

import java.util.Collection;
import java.util.Map;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

public interface CurrentPriceInfoProvider<T> {

    Map<TradeableInstrument<T>, Price<T>> getCurrentPricesForInstruments(Collection<TradeableInstrument<T>> instruments);
}
