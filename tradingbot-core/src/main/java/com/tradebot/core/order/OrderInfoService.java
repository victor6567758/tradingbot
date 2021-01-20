package com.tradebot.core.order;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.utils.TradingUtils;

//TODO: introduce a cache  like in TradeInfoService in order to avoid making expensive rest calls.
public class OrderInfoService<N, K> {

    private final OrderManagementProvider<N, K> orderManagementProvider;

    public OrderInfoService(OrderManagementProvider<N, K> orderManagementProvider) {
        this.orderManagementProvider = orderManagementProvider;
    }

    public Collection<Order<N>> allPendingOrders() {
        return this.orderManagementProvider.allPendingOrders();
    }

    public Collection<Order<N>> pendingOrdersForAccount(K accountId) {
        return this.orderManagementProvider.pendingOrdersForAccount(accountId);
    }

    public Collection<Order<N>> pendingOrdersForInstrument(TradeableInstrument instrument) {
        return this.orderManagementProvider.pendingOrdersForInstrument(instrument);
    }

    public Order<N> pendingOrderForAccount(N orderId, K accountId) {
        return this.orderManagementProvider.pendingOrderForAccount(orderId, accountId);
    }

    public int findNetPositionCountForCurrency(String currency) {
        Collection<Order<N>> allOrders = allPendingOrders();
        int positionCount = 0;
        for (Order<N> order : allOrders) {
            positionCount += TradingUtils.getSign(order.getInstrument().getInstrument(), order.getSide(), currency);
        }
        return positionCount;
    }
}
