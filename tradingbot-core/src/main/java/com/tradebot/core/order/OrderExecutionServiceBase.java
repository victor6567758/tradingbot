package com.tradebot.core.order;

import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.OrderExecutionServiceCallback;
import com.tradebot.core.model.TradingDecision;
import com.tradebot.core.utils.CommonUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public Future<List<Order<N>>> submit(List<Order<N>> orders) {
        return executorService.submit(() -> processOrderList(orders));
    }

    public Future<Optional<Order<N>>> submit(Order<N> order) {
        return executorService.submit(() -> processOrder(order));
    }

    public Future<List<Order<N>>> submit(List<Order<N>> orders, int delaySec) {
        return executorService.schedule(() -> processOrderList(orders), delaySec, TimeUnit.SECONDS);
    }

    public Future<Optional<Order<N>>> submit(Order<N> order, int delaySec) {
        return executorService.schedule(() -> processOrder(order), delaySec, TimeUnit.SECONDS);
    }

    public void shutdown() {
        CommonUtils.commonExecutorServiceShutdown(executorService, SHUTDOWN_WAIT_TIME);
    }

    public abstract List<Order<N>> createOrderListFromDecision(TradingDecision<C> decision, Function<C, N> mapperToClientOrderId);

    protected abstract boolean preValidate(TradingDecision<C> decision);

    private Optional<Order<N>> processOrder(Order<N> order) {

        if (!initOrderSubmit()) {
            return Optional.empty();
        }

        OperationResultContext<N> result = orderManagementProvider.placeOrder(order, accountIdSupplier.get());
        order.setOrderId(result.getData());

        orderExecutionServiceCallback.onOperationResult(result);
        return Optional.of(order);
    }

    private List<Order<N>> processOrderList(List<Order<N>> orders) {

        if (!initOrderSubmit()) {
            return Collections.emptyList();
        }

        orders.forEach(order -> {
            OperationResultContext<N> result = orderManagementProvider.placeOrder(order, accountIdSupplier.get());
            order.setOrderId(result.getData());

            orderExecutionServiceCallback.onOperationResult(result);
        });

        return orders;
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
