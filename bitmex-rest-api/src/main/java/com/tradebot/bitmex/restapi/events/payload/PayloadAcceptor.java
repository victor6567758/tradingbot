package com.tradebot.bitmex.restapi.events.payload;

public interface PayloadAcceptor {
    void accept(ProcessedEventVisitor visitor);
}
