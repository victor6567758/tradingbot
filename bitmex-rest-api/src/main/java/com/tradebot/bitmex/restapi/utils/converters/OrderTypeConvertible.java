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
            case MARKET_IF_TOUCHED: return "MarketIfTouched";
            case LIMIT_IF_TOUCHED: return "LimitIfTouched";
            case MARKET_WITH_LEFTOVER_AS_LIMIT: return "MarketWithLeftOverAsLimit";
            case PEGGED: return "Pegged";

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
        } else if ("MarketIfTouched".equals(value)) {
            return OrderType.MARKET_IF_TOUCHED;
        } else if ("LimitIfTouched".equals(value)) {
            return OrderType.LIMIT_IF_TOUCHED;
        } else if ("MarketWithLeftOverAsLimit".equals(value)) {
            return OrderType.MARKET_WITH_LEFTOVER_AS_LIMIT;
        } else if ("Pegged".equals(value)) {
            return OrderType.PEGGED;
        }

        throw new IllegalArgumentException();
    }
}
