
package com.tradebot.core.position;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface PositionManagementProvider<M, N> {

    Position<M> getPositionForInstrument(N accountId, TradeableInstrument<M> instrument);

    Collection<Position<M>> getPositionsForAccount(N accountId);

    boolean closePosition(N accountId, TradeableInstrument<M> instrument, double price);

}
