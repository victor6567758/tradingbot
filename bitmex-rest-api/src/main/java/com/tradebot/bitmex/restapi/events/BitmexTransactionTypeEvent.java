package com.tradebot.bitmex.restapi.events;

import com.tradebot.core.events.Event;

public enum BitmexTransactionTypeEvent implements Event {
    REALIZED_PNL("RealisedPNL"),
    TRANSFER("Transfer");

    public final String label;

    BitmexTransactionTypeEvent(String label) {
        this.label = label;
    }


    @Override
    public String label() {
        return label;
    }

}
