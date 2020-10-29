
package com.tradebot.bitmex.restapi.events;


import com.tradebot.core.events.Event;

public enum TradeEvents implements Event {
    EVENT_INSURANCE,
    EVENT_ANNOUNCEMENT,
    EVENT_PUBLIC_NOTIFICATION,

    BITMEX_INSTRUMENT,

    TRADE_CLOSE,
    STOP_LOSS_FILLED,
    TAKE_PROFIT_FILLED,

    MIGRATE_TRADE_CLOSE,

    NONE;

    public final String label;

    TradeEvents(String label) {
        this.label = label;
    }

    TradeEvents() {
        this.label = null;
    }


    @Override
    public String label() {
        return label;
    }

}
