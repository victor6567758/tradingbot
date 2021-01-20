
package com.tradebot.core.trade;

import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Getter;
import org.joda.time.DateTime;

@Getter
public class Trade<M, K> {

    private final M tradeId;
    private final long units;
    private final TradingSignal side;
    private final TradeableInstrument instrument;
    private final DateTime tradeDate;
    private final double takeProfitPrice;
    private final double executionPrice;
    private final double stopLoss;
    private final K accountId;
    private final String toStr;

    public Trade(
        M tradeId,
        long units,
        TradingSignal side,
        TradeableInstrument instrument,
        DateTime tradeDate,
        double takeProfitPrice,
        double executionPrice,
        double stopLoss,
        K accountId) {

        this.tradeId = tradeId;
        this.units = units;
        this.side = side;
        this.instrument = instrument;
        this.tradeDate = tradeDate;
        this.takeProfitPrice = takeProfitPrice;
        this.executionPrice = executionPrice;
        this.stopLoss = stopLoss;
        this.accountId = accountId;
        toStr = String.format(
            "Trade Id=%s, Units=%d, Side=%s, Instrument=%s, TradeDate=%s, TP=%3.5f, Price=%3.5f, SL=%3.5f",
            tradeId.toString(), units, side, instrument, tradeDate.toString(), takeProfitPrice, executionPrice, stopLoss);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((tradeId == null) ? 0 : tradeId.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Trade<M, K> other = (Trade<M, K>) obj;
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        if (tradeId == null) {
            return other.tradeId == null;
        } else {
            return tradeId.equals(other.tradeId);
        }
    }

    @Override
    public String toString() {
        return toStr;
    }

}
