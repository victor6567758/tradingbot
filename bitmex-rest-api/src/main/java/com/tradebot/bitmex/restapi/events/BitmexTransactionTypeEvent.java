package com.tradebot.bitmex.restapi.events;

import com.tradebot.core.events.Event;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BitmexTransactionTypeEvent implements Event {
    REALIZED_PNL("RealisedPNL"),
    TRANSFER("Transfer");

    public final String label;

    @Override
    public String label() {
        return label;
    }

}
