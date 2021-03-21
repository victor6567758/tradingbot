package com.tradebot.core.order;

import com.tradebot.core.model.OperationResultContext;

public interface OrderExecutionServiceCallback<N> {

    void fired();

    boolean ifTradeAllowed();

    String getReason();

    void onOrderResult(OperationResultContext<N> operationResultContext);

}
