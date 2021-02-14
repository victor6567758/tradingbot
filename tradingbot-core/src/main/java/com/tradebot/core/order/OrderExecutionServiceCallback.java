package com.tradebot.core.order;

public interface OrderExecutionServiceCallback {

    void fired();

    boolean ifTradeAllowed();

    String getReason();

    void onOrderResult(OrderResultContext orderResultContext);

}
