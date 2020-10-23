package com.tradebot.core.account.transaction;

import com.tradebot.core.TradingSignal;
import com.tradebot.core.events.Event;
import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

@Getter
@ToString
public class Transaction<M, N, T> {

    private final M transactionId;
    private final Event transactionType;
    private final N accountId;
    private final TradeableInstrument<T> instrument;
    private final Long units;
    private final DateTime transactionTime;
    private final Double price;
    private final Double interest;
    private final Double pnl;
    private final TradingSignal side;
    @Setter
    private M linkedTransactionId;

    public Transaction(
        M transactionId,
        Event transactionType,
        N accountId,
        String instrument,
        Long units,
        TradingSignal side,
        DateTime transactionTime,
        Double price,
        Double interest,
        Double pnl) {

        super();
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.accountId = accountId;
        this.instrument = new TradeableInstrument<>(instrument);
        this.units = units;
        this.side = side;
        this.transactionTime = transactionTime;
        this.price = price;
        this.interest = interest;
        this.pnl = pnl;
    }
}
