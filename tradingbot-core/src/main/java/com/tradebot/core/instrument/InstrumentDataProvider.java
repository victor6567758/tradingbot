
package com.tradebot.core.instrument;

import java.util.Collection;


public interface InstrumentDataProvider {

    Collection<TradeableInstrument> getInstruments();
}
