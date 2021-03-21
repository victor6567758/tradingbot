package com.tradebot.core.order;

import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface OrderManagementProvider<N, K> {

    OperationResultContext<N> placeOrder(Order<N> order, K accountId);

    OperationResultContext<N> modifyOrder(Order<N> order, K accountId);

    OperationResultContext<N> closeOrder(N orderId, K accountId);

    OperationResultContext<Collection<Order<N>>> allPendingOrders();

    OperationResultContext<Collection<Order<N>>> pendingOrdersForAccount(K accountId);

    OperationResultContext<Order<N>> pendingOrderForAccount(N orderId, K accountId);

    OperationResultContext<Collection<Order<N>>> pendingOrdersForInstrument(TradeableInstrument instrument);

}
