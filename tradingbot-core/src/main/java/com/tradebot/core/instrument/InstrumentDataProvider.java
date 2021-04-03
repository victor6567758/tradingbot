
package com.tradebot.core.instrument;

import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;


public interface InstrumentDataProvider {

    OperationResultContext<Collection<TradeableInstrument>> getInstruments();
}
