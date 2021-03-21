
package com.tradebot.core.position;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface PositionManagementProvider<K> {

    Position getPositionForInstrument(K accountId, TradeableInstrument instrument);

    Collection<Position> getPositionsForAccount(K accountId);

    boolean closePosition(K accountId, TradeableInstrument instrument, double price);

}
