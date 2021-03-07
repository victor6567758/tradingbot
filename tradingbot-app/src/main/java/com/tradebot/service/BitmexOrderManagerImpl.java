package com.tradebot.service;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
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
import com.tradebot.model.TradingDecisionContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongPredicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BitmexOrderManagerImpl implements BitmexOrderManager {

    private final OrderManagementProvider<String, Long> orderManagementProvider;
    private final BitmexTradingBot bitmexTradingBot;

    private OrderExecutionServiceBase<String, Long, TradingDecisionContext> orderExecutionEngine;
    private BitmexAccountConfiguration bitmexAccountConfiguration;

    private AtomicReference<DateTime> lastOrderFireTime = new AtomicReference<>();
    private Map<String, TradingDecisionContext> clientOrderIdLevelMap = new ConcurrentHashMap<>();

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

        TradingDecisionContext tradingDecisionContext = clientOrderIdLevelMap.get(bitmexExecution.getClOrdID());
        if (tradingDecisionContext == null) {
            log.warn("Alien order cannot be processed {}", bitmexExecution.getOrderID());
            return;
        }

        int clientOrderId = tradingDecisionContext.getLevel();

        Map<Integer, Long> imbalanceMap = tradingContext.getRecalculatedTradingContext().getImbalanceMap();
        TradingDecision<TradingDecisionContext> openTradingDecision = tradingContext.getRecalculatedTradingContext().getOpenTradingDecisions().get(clientOrderId);

        List<BitmexExecution> executionList = tradingContext.getRecalculatedTradingContext().getExecutionChains()
            .computeIfAbsent(clientOrderId, integer -> new ArrayList<>());

        executionList.add(bitmexExecution);

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

                    log.info("Long filled {}", clientOrderId);

                    if (updateVolumeAndCheck(imbalanceMap, bitmexExecution, clientOrderId, qty -> qty == bitmexExecution.getOrderQty())) {
                        log.info("Long volume reached the level to open short order {}", clientOrderId);
                        removeClientOrder(bitmexExecution.getClOrdID());

                        commandToOpenCloseOrder(bitmexExecution, tradingContext, openTradingDecision, clientOrderId);
                    }

                } else {
                    log.info("Short filled {}", clientOrderId);

                    if (updateVolumeAndCheck(imbalanceMap, bitmexExecution, clientOrderId, qty -> qty == 0)) {
                        log.info("Short volume reached the level to stop the trade {}", clientOrderId);
                        removeClientOrder(bitmexExecution.getClOrdID());

                        bitmexTradingBot.setGlobalTradesEnabled(false);
                        //bitmexTradingBot.resetTradingContext();
                        bitmexTradingBot.cancelAllPendingOrders();

                    }
                }
            }
        }

    }

    @Override
    public void onOrderResultCallback(TradingContext tradingContext, OrderResultContext<String> orderResultContext) {

    }

    @Override
    public Collection<Order<String>> cancelAllPendingOrders() {
        OrderResultContext<Collection<Order<String>>> pendingOrders = orderManagementProvider.allPendingOrders();
        if (!pendingOrders.isResult()) {
            throw new IllegalArgumentException(String.format("Invalid pending order retrieval %s", pendingOrders.getMessage()));
        }

        pendingOrders.getData().forEach(order -> orderManagementProvider.closeOrder(order.getOrderId(), 1L));
        return pendingOrders.getData();
    }

    private void removeClientOrder(String clientOrderId) {
        if (clientOrderIdLevelMap.remove(clientOrderId) == null) {
            log.error("Could not find previous client order id {}", clientOrderId);
        }
    }

    private void commandToOpenCloseOrder(
        BitmexExecution bitmexExecution,
        TradingContext tradingContext,
        TradingDecision<TradingDecisionContext> openTradingDecision,
        int clientOrderId) {

        double profitPrice = BitmexUtils.roundPrice(tradingContext.getImmutableTradingContext().getTradeableInstrument(),
            bitmexExecution.getLastPx() + tradingContext.getRecalculatedTradingContext().getProfitPlus());

        Order<String> closeOrder = Order.buildStopMarketIfTouchedOrder(
            tradingContext.getImmutableTradingContext().getTradeableInstrument(),
            openTradingDecision.getUnits(),
            TradingSignal.SHORT,
            profitPrice,
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE
        );

        closeOrder.setClientOrderId(UUID.randomUUID().toString());
        clientOrderIdLevelMap.put(closeOrder.getClientOrderId(), new TradingDecisionContext(clientOrderId));

        // TODO - still need to check the validity of bitmexExecution.getLastPx()
        log.info("About to submit closing SHORT order {}, execution price for calculation the target is {}, profit plus {}",
            closeOrder.toString(),
            bitmexExecution.getLastPx(),
            tradingContext.getRecalculatedTradingContext().getProfitPlus());
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

    private void submitDecisionHelper(TradingDecision<TradingDecisionContext> decision) {
        List<Order<String>> orders = orderExecutionEngine.createOrderListFromDecision(decision);
        if (orders.size() != 1) {
            throw new IllegalArgumentException("At this strategy 1 order per a single decision");
        }

        Order<String> order = orders.get(0);
        order.setClientOrderId(UUID.randomUUID().toString());
        clientOrderIdLevelMap.put(order.getClientOrderId(), decision.getContext());

        orderExecutionEngine.submit(orders.get(0));
    }


}
