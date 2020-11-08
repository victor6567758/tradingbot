package com.tradebot.bitmex.restapi.utils.converters;

import com.tradebot.core.order.OrderType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderTypeConvertible {
    public static String toString(OrderType orderType) {
        switch(orderType) {
            case MARKET: return "Market";
            case LIMIT: return "Limit";
            case STOP_MARKET: return "Stop";
            case STOP_LIMIT: return "StopLimit";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static OrderType fromString(String value) {
        if ("Market".equals(value)) {
            return OrderType.MARKET;
        } else if ("Limit".equals(value)) {
            return OrderType.LIMIT;
        } else if ("Stop".equals(value)) {
            return OrderType.STOP_MARKET;
        } else if ("StopLimit".equals(value)) {
            return OrderType.STOP_LIMIT;
        }
        throw new IllegalArgumentException();
    }
}
