
package com.precioustech.fxtrading.instrument;

import java.util.Collection;


public interface InstrumentDataProvider<T> {

    Collection<TradeableInstrument<T>> getInstruments();
}
