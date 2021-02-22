package com.tradebot.service;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.ExecutionType;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderExecutionServiceBase;
import com.tradebot.core.order.OrderExecutionServiceCallback;
import com.tradebot.core.order.OrderExecutionSimpleServiceImpl;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.utils.CommonConsts;
import com.tradebot.model.TradingContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongPredicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BitmexOrderManagerImpl implements BitmexOrderManager {

    private final OrderManagementProvider<String, Long> orderManagementProvider;
    private final BitmexTradingBot bitmexTradingBot;

    private OrderExecutionServiceBase<String, Long> orderExecutionEngine;
    private BitmexAccountConfiguration bitmexAccountConfiguration;
    private AtomicReference<DateTime> lastOrderFireTime = new AtomicReference<>();

    @Override
    public void initialize(long accountId, BitmexAccountConfiguration bitmexAccountConfiguration) {

        orderExecutionEngine = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountId,
            new OrderExecutionServiceCallback<String>() {

                @Override
                public void fired() {
                    lastOrderFireTime.set(DateTime.now());
                }

                @Override
                public boolean ifTradeAllowed() {
                    return true;
                }

                @Override
                public String getReason() {
                    return null;
                }

                @Override
                public void onOrderResult(OrderResultContext<String> orderResultContext) {
                    bitmexTradingBot.onOrderResult(orderResultContext);
                }
            });

        this.bitmexAccountConfiguration = bitmexAccountConfiguration;
    }

    @Override
    public void submitOrder(Order<String> order) {
        orderExecutionEngine.submit(order);
    }

    @Override
    public void startOrderEvolution(TradingContext tradingContext) {
        tradingContext.getRecalculatedTradingContext().getOpenTradingDecisions().values().forEach(
            this::submitDecisionHelper);
    }

    @Override
    public void onCandleCallback(CandleStick candleStick, CacheCandlestick cacheCandlestick, TradingContext tradingContext) {
        if (log.isDebugEnabled()) {
            log.debug("Candle callback {}", candleStick.toString());
        }
    }

    @Override
    public void onOrderCallback(TradingContext tradingContext, BitmexOrderEventPayload event) {
        log.info("Order callback {}", event.getPayLoad().toString());
    }

    @Override
    @SneakyThrows
    public void onOrderExecutionCallback(TradingContext tradingContext, BitmexExecutionEventPayload event) {
        BitmexExecution bitmexExecution = event.getPayLoad();
        log.info("Order execution callback {}", bitmexExecution.toString());

        if (!event.getPayLoad().getSymbol().equals(tradingContext.getImmutableTradingContext().getTradeableInstrument().getInstrument())) {
            return;
        }

        int clientOrderId = NumberUtils.toInt(bitmexExecution.getClOrdID(), -1);

        if (clientOrderId < 0) {
            log.warn("Alien order cannot be processed {}", bitmexExecution.getOrderID());
            return;
        }

        Map<Integer, Long> imbalanceMap = tradingContext.getRecalculatedTradingContext().getImbalanceMap();
        TradingDecision openTradingDecision = tradingContext.getRecalculatedTradingContext().getOpenTradingDecisions().get(clientOrderId);

        if (bitmexExecution.getExecType() == ExecutionType.NEW) {
            if (bitmexExecution.getSide() == TradingSignal.LONG) {
                log.info("Long accepted for order {}", clientOrderId);
                imbalanceMap.put(clientOrderId, 0L);
            } else {
                log.info("Short accepted for order {}", clientOrderId);
            }

        } else if (bitmexExecution.getExecType() == ExecutionType.TRADE) {
            if (bitmexExecution.getOrdStatus() == OrderStatus.FILLED || bitmexExecution.getOrdStatus() == OrderStatus.PARTIALLY_FILLED) {

                if (bitmexExecution.getSide() == TradingSignal.LONG) {
                    // first order
                    log.info("Long filled {}", clientOrderId);

                    if (updateVolumeAndCheck(imbalanceMap, bitmexExecution, clientOrderId, qty -> qty == bitmexExecution.getOrderQty())) {
                        log.info("Long volume reached the level to open short order {}", clientOrderId);
                        commandToOpenCloseOrder(tradingContext, openTradingDecision, clientOrderId);
                    }

                } else {
                    log.info("Short filled {}, restarting the cycle", clientOrderId);

                    if (updateVolumeAndCheck(imbalanceMap, bitmexExecution, clientOrderId, qty -> qty == 0)) {
                        if (tradingContext.getRecalculatedTradingContext().isTradeEnabled()) {
                            submitDecisionHelper(openTradingDecision);
                        } else {
                            log.warn("Global trading is disabled");
                        }

                    }
                }
            }
        }

    }

    @Override
    public void onOrderResultCallback(TradingContext tradingContext, OrderResultContext<String> orderResultContext) {
        if (orderResultContext instanceof BitmexOrderQuotas) {
            BitmexOrderQuotas bitmexOrderQuotas = (BitmexOrderQuotas) orderResultContext;
            log.info("Order result (order quotas) callback {}", bitmexOrderQuotas.toString());
        } else {
            log.info("Order result callback {}", orderResultContext.toString());
        }
    }

    @Override
    public Collection<Order<String>> cancelAllPendingOrders() {
        OrderResultContext<Collection<Order<String>>> pendingOrders = orderManagementProvider.allPendingOrders();
        if (!pendingOrders.isResult()) {
            throw new IllegalArgumentException(String.format("Invalid pending order retreival %s", pendingOrders.getMessage()));
        }

        pendingOrders.getData().forEach(order -> orderManagementProvider.closeOrder(order.getOrderId(), 1L));
        return pendingOrders.getData();
    }

    private void commandToOpenCloseOrder(TradingContext tradingContext, TradingDecision openTradingDecision, int clientOrderId) {
        Order<String> closeOrder = Order.buildStopMarketOrder(
            tradingContext.getImmutableTradingContext().getTradeableInstrument(),
            openTradingDecision.getUnits(),
            TradingSignal.SHORT,
            BitmexUtils.roundPrice(tradingContext.getImmutableTradingContext().getTradeableInstrument(),
                openTradingDecision.getLimitPrice() + tradingContext.getRecalculatedTradingContext().getProfitPlus()),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE
        );

        closeOrder.setClientOrderId(String.valueOf(clientOrderId));
        log.info("About to submit closing order {}", closeOrder.toString());
        orderExecutionEngine.submit(closeOrder);
    }

    private boolean updateVolumeAndCheck(
        Map<Integer, Long> imbalanceMap,
        BitmexExecution bitmexExecution,
        int clientOrderId,
        LongPredicate volumePredicate) {
        long executedVolumeSigned = bitmexExecution.getSide() == TradingSignal.LONG ? bitmexExecution.getOrderQty() : -bitmexExecution.getOrderQty();
        long totalVolume = imbalanceMap.get(clientOrderId) + executedVolumeSigned;
        imbalanceMap.put(clientOrderId, totalVolume);
        return volumePredicate.test(totalVolume);
    }

    private void submitDecisionHelper(TradingDecision decision) {
        List<Order<String>> orders = orderExecutionEngine.createOrderListFromDecision(decision);
        orders.forEach( order -> {
            orderExecutionEngine.submit(order);
        });

    }


}
