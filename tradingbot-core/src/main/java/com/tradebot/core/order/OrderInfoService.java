package com.tradebot.core.order;

import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;

import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.utils.TradingUtils;
import lombok.RequiredArgsConstructor;

//TODO: introduce a cache  like in TradeInfoService in order to avoid making expensive rest calls.
@RequiredArgsConstructor
public class OrderInfoService<N, K> {

    private static final String INVALID_ORDER_PROVIDER_RESULT_S = "Invalid order provider result: %s";
    private final OrderManagementProvider<N, K> orderManagementProvider;
    private final OperationResultCallback operationResultCallback;

    public Collection<Order<N>> allPendingOrders() {
        OperationResultContext<Collection<Order<N>>> pendingOrderResult = orderManagementProvider.allPendingOrders();
        operationResultCallback.onOrderResult(pendingOrderResult);
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
    }

    public Collection<Order<N>> pendingOrdersForAccount(K accountId) {
        OperationResultContext<Collection<Order<N>>> pendingOrderResult = orderManagementProvider.pendingOrdersForAccount(accountId);
        operationResultCallback.onOrderResult(pendingOrderResult);
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
    }

    public Collection<Order<N>> pendingOrdersForInstrument(TradeableInstrument instrument) {
        OperationResultContext<Collection<Order<N>>> pendingOrderResult = orderManagementProvider.pendingOrdersForInstrument(instrument);
        operationResultCallback.onOrderResult(pendingOrderResult);
        if (pendingOrderResult.isResult()) {
            return pendingOrderResult.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ORDER_PROVIDER_RESULT_S, pendingOrderResult.getMessage()));
    }

    public Order<N> pendingOrderForAccount(N orderId, K accountId) {
        OperationResultContext<Order<N>> pendingOrderResult = orderManagementProvider.pendingOrderForAccount(orderId, accountId);
        operationResultCallback.onOrderResult(pendingOrderResult);
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
