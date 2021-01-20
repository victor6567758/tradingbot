
package com.tradebot.core.position;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface PositionManagementProvider<N> {

    Position getPositionForInstrument(N accountId, TradeableInstrument instrument);

    Collection<Position> getPositionsForAccount(N accountId);

    boolean closePosition(N accountId, TradeableInstrument instrument, double price);

}
