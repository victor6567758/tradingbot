package com.tradebot.bitmex.restapi.utils.converters;

import com.tradebot.core.model.TradingSignal;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class TradingSignalConvertible {
    public static String toString(TradingSignal tradingSignal) {
        switch(tradingSignal) {
            case LONG: return "Buy";
            case SHORT: return "Sell";
            case NONE: return "None";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static TradingSignal fromString(String value) {
        if (StringUtils.isBlank(value) || "None".equalsIgnoreCase(value)) {
            return TradingSignal.NONE;
        }
        if ("Buy".equals(value)) {
            return TradingSignal.LONG;
        } else if ("Sell".equals(value)) {
            return TradingSignal.SHORT;
        }
        throw new IllegalArgumentException();
    }
}
