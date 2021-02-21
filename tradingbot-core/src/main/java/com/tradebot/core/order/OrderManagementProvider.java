package com.tradebot.core.order;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;


public interface OrderManagementProvider<N, K> {

    OrderResultContext<N> placeOrder(Order<N> order, K accountId);

    OrderResultContext<N> modifyOrder(Order<N> order, K accountId);

    OrderResultContext<N> closeOrder(N orderId, K accountId);

    OrderResultContext<Collection<Order<N>>> allPendingOrders();

    OrderResultContext<Collection<Order<N>>> pendingOrdersForAccount(K accountId);

    OrderResultContext<Order<N>> pendingOrderForAccount(N orderId, K accountId);

    OrderResultContext<Collection<Order<N>>> pendingOrdersForInstrument(TradeableInstrument instrument);

}
