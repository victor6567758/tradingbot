package com.tradebot.core.marketdata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.joda.time.DateTime;

import com.tradebot.core.instrument.TradeableInstrument;

@RequiredArgsConstructor
@Getter
@ToString
public class Price {
    private final TradeableInstrument instrument;
    private final double bidPrice;
    private final double askPrice;
    private final DateTime pricePoint;
}
