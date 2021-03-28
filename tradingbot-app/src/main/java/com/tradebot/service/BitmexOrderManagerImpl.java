package com.tradebot.service;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.model.ExecutionType;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.OrderExecutionServiceCallback;
import com.tradebot.core.model.TradingDecision;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderExecutionServiceBase;
import com.tradebot.core.order.OrderExecutionSimpleServiceImpl;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.position.PositionManagementProvider;
import com.tradebot.core.position.PositionService;
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
import org.joda.time.Instant;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BitmexOrderManagerImpl implements BitmexOrderManager {

    private final OrderManagementProvider<String, Long> orderManagementProvider;
    private final PositionManagementProvider<Long> positionManagementProvider;
    private final BitmexTradingBot bitmexTradingBot;

    private OrderExecutionServiceBase<String, Long, TradingDecisionContext> orderExecutionEngine;
    private PositionService<Long> positionService;
    private BitmexAccountConfiguration bitmexAccountConfiguration;

    private AtomicReference<DateTime> lastOrderFireTime = new AtomicReference<>();
    private Map<String, TradingDecisionContext> clientOrderIdLevelMap = new ConcurrentHashMap<>();

    @Override
    public void initialize(long accountId, BitmexAccountConfiguration bitmexAccountConfiguration) {

        orderExecutionEngine = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountId,
            new OrderExecutionServiceCallback() {

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
                public void onOrderResult(OperationResultContext<?> operationResultContext) {
                    bitmexTradingBot.onOperationResult(operationResultContext);
                }
            });

        positionService = new PositionService<>(positionManagementProvider,
            bitmexTradingBot::onOperationResult);

        this.bitmexAccountConfiguration = bitmexAccountConfiguration;
    }

    @Override
    public void submitOrder(Order<String> order) {
        orderExecutionEngine.submit(order);
    }

    @Override
    public boolean startOrderEvolution(TradingContext tradingContext) {

        log.info(">>>> START EVOLUTION >>>>");
        log.info("Profit plus {}", tradingContext.getRecalculatedTradingContext().getProfitPlus());
        tradingContext.getRecalculatedTradingContext().getOpenTradingDecisions().values().forEach(decision -> {
            log.info("Trading decision {}, {}", decision.getContext().getLevel(), decision.toString());
            submitDecisionHelper(tradingContext, decision);
        });
        log.info(">>>>>>>>>>>>>>>");

        return true;
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

        int resolvedLevel = resolveClientLevel(bitmexExecution, tradingContext, event);
        if (resolvedLevel < 0) {
            return;
        }

        Map<Integer, Long> imbalanceMap = tradingContext.getRecalculatedTradingContext().getImbalanceMap();
        TradingDecision<TradingDecisionContext> openTradingDecision = tradingContext
            .getRecalculatedTradingContext().getOpenTradingDecisions().get(resolvedLevel);

        List<BitmexExecution> executionList = tradingContext.getRecalculatedTradingContext().getExecutionChains()
            .computeIfAbsent(resolvedLevel, integer -> new ArrayList<>());

        executionList.add(bitmexExecution);

        if (bitmexExecution.getExecType() == ExecutionType.NEW) {
            if (bitmexExecution.getSide() == TradingSignal.LONG) {
                log.info("Long accepted for order {}", resolvedLevel);
                imbalanceMap.put(resolvedLevel, 0L);
            } else {
                log.info("Short accepted for order {}", resolvedLevel);
            }

        } else if (bitmexExecution.getExecType() == ExecutionType.TRADE) {
            if (bitmexExecution.getOrdStatus() == OrderStatus.FILLED || bitmexExecution.getOrdStatus() == OrderStatus.PARTIALLY_FILLED) {

                if (bitmexExecution.getSide() == TradingSignal.LONG) {

                    log.info("Long filled {}", resolvedLevel);

                    if (updateVolumeAndCheck(imbalanceMap, bitmexExecution, resolvedLevel, qty -> qty == bitmexExecution.getOrderQty())) {
                        log.info("Long volume reached the level to open short order {}", resolvedLevel);
                        removeClientOrderReference(bitmexExecution.getClOrdID());

                        commandToOpenCloseOrder(bitmexExecution, tradingContext, openTradingDecision, resolvedLevel);
                    }

                } else {
                    log.info("Short filled {}", resolvedLevel);

                    if (updateVolumeAndCheck(imbalanceMap, bitmexExecution, resolvedLevel, qty -> qty == 0)) {
                        log.info("Short volume reached the level to stop the trade {}", resolvedLevel);
                        removeClientOrderReference(bitmexExecution.getClOrdID());

                        // level 0 - need to restart trading
                        // other levels need to repeat level scenario
                        if (resolvedLevel == 0) {
                            log.info("Restart trading fully as zero level");

                            // TODO  - need to make sure this done within 1 min, otherwise it's bad design
                            bitmexTradingBot.cancelAllPendingOrders();
                            bitmexTradingBot.resetTradingContext();
                        } else {
                            log.info("Restart trading at level {} only", resolvedLevel);
                            submitDecisionHelper(tradingContext, openTradingDecision);
                        }
                    }
                }
            }
        }

    }

    @Override
    public Collection<Order<String>> cancelAllPendingOrders() {
        OperationResultContext<Collection<Order<String>>> pendingOrders = orderManagementProvider.allPendingOrders();
        if (!pendingOrders.isResult()) {
            throw new IllegalArgumentException(String.format("Invalid pending order retrieval %s", pendingOrders.getMessage()));
        }

        pendingOrders.getData().forEach(order -> orderManagementProvider.closeOrder(order.getOrderId(), 1L));
        return pendingOrders.getData();
    }

    private int resolveClientLevel(BitmexExecution bitmexExecution, TradingContext tradingContext, BitmexExecutionEventPayload event) {

        if (!event.getPayLoad().getSymbol().equals(tradingContext.getImmutableTradingContext().getTradeableInstrument().getInstrument())) {
            log.warn("Non relevant symbol {}", event.getPayLoad().getSymbol());
            return -1;
        }

        TradingDecisionContext tradingDecisionContext = clientOrderIdLevelMap.get(bitmexExecution.getClOrdID());
        if (tradingDecisionContext == null) {
            log.warn("Alien order cannot be processed {}", bitmexExecution.getOrderID());
            return -1;
        }

        return tradingDecisionContext.getLevel();
    }

    private void removeClientOrderReference(String clientOrderId) {
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

        Order<String> closeOrder = Order.buildStopLimitIfTouchedOrder(
            tradingContext.getImmutableTradingContext().getTradeableInstrument(),
            openTradingDecision.getUnits(),
            TradingSignal.SHORT,
            profitPrice,
            profitPrice,
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE
        );

        submitOrderHelper(tradingContext, closeOrder, new TradingDecisionContext(clientOrderId));
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


    private void submitDecisionHelper(TradingContext tradingContext, TradingDecision<TradingDecisionContext> decision) {
        List<Order<String>> orders = orderExecutionEngine.createOrderListFromDecision(decision);
        if (orders.size() != 1) {
            throw new IllegalArgumentException("At this strategy 1 order per a single decision");
        }

        submitOrderHelper(tradingContext, orders.get(0), decision.getContext());
    }

    private void submitOrderHelper(TradingContext tradingContext, Order<String> order, TradingDecisionContext tradingDecisionContext) {
        BitmexOperationQuotas<?> currentOrderLimitation = tradingContext.getRecalculatedTradingContext().getBitmexOrderQuotas();
        if (currentOrderLimitation != null) {
            if (log.isDebugEnabled()) {
                log.debug("Bitmex order time restriction, reset time {}, {}",
                    Instant.ofEpochMilli(currentOrderLimitation.getXRatelimitReset()).toString(), currentOrderLimitation.toString());
            }
        }

        order.setClientOrderId(UUID.randomUUID().toString());
        clientOrderIdLevelMap.put(order.getClientOrderId(), tradingDecisionContext);

        log.info("About to submit order {}, profit plus {}",
            order.toString(),
            tradingContext.getRecalculatedTradingContext().getProfitPlus());

        orderExecutionEngine.submit(order);
    }

}
