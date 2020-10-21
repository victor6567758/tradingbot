package com.tradebot.bitmex.restapi.events;


import com.tradebot.core.events.Event;

public enum OrderEvents implements Event {
    MARKET_ORDER_CREATE,
    STOP_ORDER_CREATE,
    LIMIT_ORDER_CREATE,
    MARKET_IF_TOUCHED_ORDER_CREATE,
    ORDER_UPDATE,
    ORDER_CANCEL,
    ORDER_FILLED;

}
