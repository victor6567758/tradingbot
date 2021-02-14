package com.tradebot.core.order;

public interface OrderExecutionServiceCallback<N> {

    void fired();

    boolean ifTradeAllowed();

    String getReason();

    void onOrderResult(OrderResultContext<N> orderResultContext);

}
