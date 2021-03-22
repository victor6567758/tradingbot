package com.tradebot.core.order;

import com.tradebot.core.model.OrderExecutionServiceCallback;
import com.tradebot.core.model.TradingDecision;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.utils.CommonUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class OrderExecutionServiceBase<N, K, C> {

    private static final long SHUTDOWN_WAIT_TIME = 5000L;

    private final OrderExecutionServiceCallback orderExecutionServiceCallback;
    private final OrderManagementProvider<N, K> orderManagementProvider;
    private final Supplier<K> accountIdSupplier;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public Future<List<Order<N>>> submit(TradingDecision<C> decision) {
        return executorService.submit(() -> processTradingDecision(decision));
    }

    public Future<Optional<Order<N>>> submit(Order<N> order) {
        return executorService.submit(() -> processOrder(order));
    }

    public void shutdown() {
        CommonUtils.commonExecutorServiceShutdown(executorService, SHUTDOWN_WAIT_TIME);
    }

    public abstract List<Order<N>> createOrderListFromDecision(TradingDecision<C> decision);

    protected abstract boolean preValidate(TradingDecision<C> decision);

    private Optional<Order<N>> processOrder(Order<N> order) {

        if (!initOrderSubmit()) {
            return Optional.empty();
        }

        OperationResultContext<N> result = orderManagementProvider.placeOrder(order, accountIdSupplier.get());
        order.setOrderId(result.getData());

        orderExecutionServiceCallback.onOrderResult(result);
        return Optional.of(order);
    }

    private List<Order<N>> processTradingDecision(TradingDecision<C> decision) {

        if (!initOrderSubmit()) {
            return Collections.emptyList();
        }

        if (!preValidate(decision)) {
            log.warn("Validation failed for a decision: {}", decision.toString());
            return Collections.emptyList();
        }

        List<Order<N>> ordersGenerated = createOrderListFromDecision(decision);
        ordersGenerated.forEach(order -> {
            OperationResultContext<N> result = orderManagementProvider.placeOrder(order, accountIdSupplier.get());
            order.setOrderId(result.getData());

            orderExecutionServiceCallback.onOrderResult(result);
        });
        return ordersGenerated;

    }

    private boolean initOrderSubmit() {
        if (!orderExecutionServiceCallback.ifTradeAllowed()) {
            log.warn("Trade is not allowed: {}", orderExecutionServiceCallback.getReason());
            return false;
        }

        orderExecutionServiceCallback.fired();
        return true;
    }

}
