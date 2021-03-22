package com.tradebot.core.model;

public interface OperationResultCallback {
    void onOrderResult(OperationResultContext<?> operationResultContext);
}
