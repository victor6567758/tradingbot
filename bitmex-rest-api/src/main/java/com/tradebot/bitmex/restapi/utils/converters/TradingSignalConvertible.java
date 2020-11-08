package com.tradebot.bitmex.restapi.utils.converters;

import com.tradebot.core.TradingSignal;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TradingSignalConvertible {
    public static String toString(TradingSignal tradingSignal) {
        switch(tradingSignal) {
            case LONG: return "Buy";
            case SHORT: return "Sell";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static TradingSignal fromString(String value) {
        if ("Buy".equals(value)) {
            return TradingSignal.LONG;
        } else if ("Sell".equals(value)) {
            return TradingSignal.SHORT;
        }
        throw new IllegalArgumentException();
    }
}
