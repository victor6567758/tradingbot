package com.precioustech.fxtrading.account.transaction;

import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.events.Event;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

@RequiredArgsConstructor
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
}
