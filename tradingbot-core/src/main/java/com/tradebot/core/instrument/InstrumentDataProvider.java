
package com.tradebot.core.instrument;

import java.util.Collection;


public interface InstrumentDataProvider<T> {

    Collection<TradeableInstrument<T>> getInstruments();
}
