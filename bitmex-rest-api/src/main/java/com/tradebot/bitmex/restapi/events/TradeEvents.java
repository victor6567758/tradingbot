
package com.tradebot.bitmex.restapi.events;


import com.tradebot.core.events.Event;

public enum TradeEvents implements Event {
    EVENT_INSURANCE,
    EVENT_ANNOUNCEMENT,
    EVENT_PUBLIC_NOTIFICATION,
    EVENT_INSTRUMENT,
    EVENT_ORDER,
    EVENT_TRADE,
    EVENT_TRADE_BIN,
    EVENT_EXECUTION;

    @Override
    public String label() {
        return this.name();
    }

}
