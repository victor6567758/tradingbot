package com.precioustech.fxtrading.marketdata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

@RequiredArgsConstructor
@Getter
public class Price<T> {
    private final TradeableInstrument<T> instrument;
    private final double bidPrice, askPrice;
    private final DateTime pricePoint;
}
