package com.precioustech.fxtrading.marketdata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

import com.precioustech.fxtrading.instrument.TradeableInstrument;

@RequiredArgsConstructor
@Getter
public class MarketDataPayLoad<T> {
    private final double bidPrice;
    private final double askPrice;
    private final TradeableInstrument<T> instrument;
    private final DateTime eventDate;
}
