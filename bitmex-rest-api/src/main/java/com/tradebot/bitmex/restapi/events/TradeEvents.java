
package com.tradebot.bitmex.restapi.events;


import com.tradebot.core.events.Event;

public enum TradeEvents implements Event {
    TRADE_UPDATE,
    TRADE_CLOSE,
    MIGRATE_TRADE_OPEN,
    MIGRATE_TRADE_CLOSE,
    STOP_LOSS_FILLED,
    TAKE_PROFIT_FILLED,
    TRAILING_STOP_FILLED;
}
