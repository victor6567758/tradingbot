package com.tradebot.core.order;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface OrderManagementProvider<N, K> {

    OrderResultContext<N> placeOrder(Order<N> order, K accountId);

    OrderResultContext<N> modifyOrder(Order<N> order, K accountId);

    OrderResultContext<N> closeOrder(N orderId, K accountId);

    Collection<Order<N>> allPendingOrders();

    Collection<Order<N>> pendingOrdersForAccount(K accountId);

    Order<N> pendingOrderForAccount(N orderId, K accountId);

    Collection<Order<N>> pendingOrdersForInstrument(TradeableInstrument instrument);

}
