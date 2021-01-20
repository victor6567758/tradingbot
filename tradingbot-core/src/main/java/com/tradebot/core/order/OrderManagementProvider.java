package com.tradebot.core.order;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface OrderManagementProvider<N, K> {

    N placeOrder(Order<N> order, K accountId);

    boolean modifyOrder(Order<N> order, K accountId);

    boolean closeOrder(N orderId, K accountId);

    Collection<Order<N>> allPendingOrders();

    Collection<Order<N>> pendingOrdersForAccount(K accountId);

    Order<N> pendingOrderForAccount(N orderId, K accountId);

    Collection<Order<N>> pendingOrdersForInstrument(TradeableInstrument instrument);

}
