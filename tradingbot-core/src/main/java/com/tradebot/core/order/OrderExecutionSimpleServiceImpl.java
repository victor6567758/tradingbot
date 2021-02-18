package com.tradebot.core.order;

import com.tradebot.core.TradingDecision;
import com.tradebot.core.utils.CommonConsts;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderExecutionSimpleServiceImpl<N, K> extends OrderExecutionServiceBase<N, K> {

    public OrderExecutionSimpleServiceImpl(
        OrderManagementProvider<N, K> orderManagementProvider,
        Supplier<K> accountIdSupplier,
        OrderExecutionServiceCallback orderExecutionServiceCallback) {
        super(orderExecutionServiceCallback, orderManagementProvider, accountIdSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order<N>> createOrderListFromDecision(TradingDecision decision) {

        Order<N> order;
        if (decision.getLimitPrice() == 0.0) {

            if (decision.getStopPrice() == 0.0) {
                order = Order.buildMarketOrder(decision.getInstrument(), decision.getUnits(), decision.getSignal(),
                    CommonConsts.INVALID_PRICE, CommonConsts.INVALID_PRICE);
            } else {
                order = Order.buildStopMarketOrder(decision.getInstrument(), decision.getUnits(), decision.getSignal(),
                    decision.getStopPrice(), CommonConsts.INVALID_PRICE, CommonConsts.INVALID_PRICE);
            }

        } else {
            if (decision.getStopPrice() == 0.0) {
                order = Order.buildLimitOrder(decision.getInstrument(), decision.getUnits(), decision.getSignal(),
                    decision.getLimitPrice(), CommonConsts.INVALID_PRICE, CommonConsts.INVALID_PRICE);
            } else {
                order = Order.buildStopLimitOrder(decision.getInstrument(), decision.getUnits(), decision.getSignal(),
                    decision.getStopPrice(), decision.getLimitPrice(), CommonConsts.INVALID_PRICE, CommonConsts.INVALID_PRICE);
            }
        }

        order.setClientOrderId((N) decision.getText());
        log.info("Order {} was generated based on decision {}", order.toString(), decision.toString());
        return Collections.singletonList(order);
    }

    @Override
    protected boolean preValidate(TradingDecision decision) {
        return true;
    }


}
