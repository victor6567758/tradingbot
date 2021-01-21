package com.tradebot.bitmex.restapi.events.payload;

public interface ProcessedEventVisitor {
    void visit(JsonEventPayLoad event);
    void visit(BitmexTradeEventPayload event);
    void visit(BitmexOrderEventPayload event);
    void visit(BitmexInstrumentEventPayload event);
    void visit(BitmexExecutionEventPayload event);
    void visit(BitmexTradeBinEventPayload event);
}
