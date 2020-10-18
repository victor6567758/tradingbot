
package com.precioustech.fxtrading.trade;

import lombok.Getter;
import org.joda.time.DateTime;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;

@Getter
public class Trade<M, N, K> {
    private final M tradeId;
    private final long units;
    private final TradingSignal side;
    private final TradeableInstrument<N> instrument;
    private final DateTime tradeDate;
    private final double takeProfitPrice, executionPrice, stopLoss;
    private final K accountId;
    private final String toStr;

    public Trade(M tradeId, long units, TradingSignal side, TradeableInstrument<N> instrument, DateTime tradeDate,
            double takeProfitPrice, double executionPrice, double stopLoss, K accountId) {
        this.tradeId = tradeId;
        this.units = units;
        this.side = side;
        this.instrument = instrument;
        this.tradeDate = tradeDate;
        this.takeProfitPrice = takeProfitPrice;
        this.executionPrice = executionPrice;
        this.stopLoss = stopLoss;
        this.accountId = accountId;
        this.toStr = String.format(
                "Trade Id=%d, Units=%d, Side=%s, Instrument=%s, TradeDate=%s, TP=%3.5f, Price=%3.5f, SL=%3.5f",
                tradeId, units, side, instrument, tradeDate.toString(), takeProfitPrice, executionPrice, stopLoss);
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
        Trade<M, N, K> other = (Trade<M, N, K>) obj;
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
