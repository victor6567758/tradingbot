package com.tradebot.core.marketdata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

import com.tradebot.core.instrument.TradeableInstrument;

@RequiredArgsConstructor
@Getter
public class MarketDataPayLoad {
    private final double bidPrice;
    private final double askPrice;
    private final TradeableInstrument instrument;
    private final DateTime eventDate;
}
