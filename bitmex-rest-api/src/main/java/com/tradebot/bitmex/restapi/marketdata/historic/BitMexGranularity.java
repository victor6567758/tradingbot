package com.tradebot.bitmex.restapi.marketdata.historic;

import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import lombok.Getter;

public enum BitMexGranularity {

    M1("1m", CandleStickGranularity.M1),
    M5("5m", CandleStickGranularity.M5),
    H1("1h", CandleStickGranularity.H1),
    D1("1d", CandleStickGranularity.D);

    @Getter
    private final String stringValue;
    private final CandleStickGranularity candleStickGranularity;

    BitMexGranularity(String stringValue, CandleStickGranularity candleStickGranularity) {
        this.stringValue = stringValue;
        this.candleStickGranularity = candleStickGranularity;
    }

    public static String toBitmexGranularity(CandleStickGranularity candleStickGranularity) {
        switch(candleStickGranularity) {
            case M1:
                return BitMexGranularity.M1.getStringValue();
            case M5:
                return BitMexGranularity.M5.getStringValue();
            case H1:
                return BitMexGranularity.H1.getStringValue();
            case D:
                return BitMexGranularity.D1.getStringValue();
            default:
                throw new IllegalArgumentException("Invalid granularity");
        }
    }
}
