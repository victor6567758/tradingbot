package com.tradebot.core.model;

public interface OrderExecutionServiceCallback extends OperationResultCallback {

    void fired();

    boolean ifTradeAllowed();

    String getReason();
}
