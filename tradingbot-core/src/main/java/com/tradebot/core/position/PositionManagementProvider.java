
package com.tradebot.core.position;

import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface PositionManagementProvider<K> {

    OperationResultContext<Position> getPositionForInstrument(K accountId, TradeableInstrument instrument);

    OperationResultContext<Collection<Position>> getPositionsForAccount(K accountId);

    OperationResultContext<String> closePosition(K accountId, TradeableInstrument instrument, double price);

}
