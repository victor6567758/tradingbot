package com.tradebot.core.order;

public enum OrderType {
    MARKET,
    LIMIT,
    STOP_MARKET,
    STOP_LIMIT,
    MARKET_IF_TOUCHED,
    LIMIT_IF_TOUCHED,
    MARKET_WITH_LEFTOVER_AS_LIMIT,
    PEGGED;
}
