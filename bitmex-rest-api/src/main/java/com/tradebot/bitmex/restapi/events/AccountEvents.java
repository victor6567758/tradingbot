package com.tradebot.bitmex.restapi.events;


import com.tradebot.core.events.Event;

public enum AccountEvents implements Event {
    MARGIN_CALL_ENTER,
    MARGIN_CALL_EXIT,
    MARGIN_CLOSEOUT,
    SET_MARGIN_RATE,
    TRANSFER_FUNDS,
    DAILY_INTEREST,
    FEE,
    NONE;

    public final String label;

    AccountEvents(String label) {
        this.label = label;
    }

    AccountEvents() {
        this.label = null;
    }


    @Override
    public String label() {
        return label;
    }
}
