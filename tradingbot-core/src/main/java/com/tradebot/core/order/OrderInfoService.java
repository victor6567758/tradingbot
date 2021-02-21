package com.tradebot.core.order;

import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.utils.TradingUtils;

//TODO: introduce a cache  like in TradeInfoService in order to avoid making expensive rest calls.
public class OrderInfoService<N, K> {

    private static final String INVALID_ORDER_PROVIDER_RESULT_S = "Invalid order provider result: %s";
    private final OrderManagementProvider<N, K> orderManagementProvider;

    public OrderInfoService(OrderManagementProvider<N, K> orderManagementProvider) {
        this.orderManagementProvider = orderManagementProvider;
    }

    public Collection<Order<N>> allPendingOrders() {
        OrderResultContext<Collection<Order<N>>> pendingOrderResult = orderManagementProvider.allPendingOrders();
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
    }

    public Collection<Order<N>> pendingOrdersForAccount(K accountId) {
        OrderResultContext<Collection<Order<N>>> pendingOrderResult = orderManagementProvider.pendingOrdersForAccount(accountId);
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
    }

    public Collection<Order<N>> pendingOrdersForInstrument(TradeableInstrument instrument) {
        OrderResultContext<Collection<Order<N>>> pendingOrderResult = orderManagementProvider.pendingOrdersForInstrument(instrument);
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
    }

    public Order<N> pendingOrderForAccount(N orderId, K accountId) {
        OrderResultContext<Order<N>> pendingOrderResult = orderManagementProvider.pendingOrderForAccount(orderId, accountId);
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
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
