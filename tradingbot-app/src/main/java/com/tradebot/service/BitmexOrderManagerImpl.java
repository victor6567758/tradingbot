package com.tradebot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.core.ExecutionType;
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
import com.tradebot.model.OrderContext;
import com.tradebot.model.TradingContext;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        tradingContext.getRecalculatedTradingContext().getOpenTradingDecisions().values().forEach(decision -> {
            orderExecutionEngine.submit(decision);
        });
    }

    @Override
    public void onCandleCallback(CandleStick candleStick, CacheCandlestick cacheCandlestick, TradingContext tradingContext) {

        if (log.isDebugEnabled()) {
            log.debug("Candle callback {}", candleStick.toString());
        }

        if (tradingContext.getRecalculatedTradingContext().isTradeEnabled()) {

        }

    }

    @Override
    public void onOrderCallback(TradingContext tradingContext, BitmexOrderEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order callback {}", event.getPayLoad().toString());
        }
    }

    @Override
    @SneakyThrows
    public void onOrderExecutionCallback(TradingContext tradingContext, BitmexExecutionEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order execution callback {}", event.getPayLoad().toString());
        }

        if (!event.getPayLoad().getSymbol().equals(tradingContext.getImmutableTradingContext().getTradeableInstrument().getInstrument())) {
            return;
        }

        BitmexExecution bitmexExecution = event.getPayLoad();
        OrderContext orderContext;

        if (StringUtils.isNotEmpty(bitmexExecution.getClOrdID())) {
            ObjectMapper mapper = new ObjectMapper();
            orderContext = mapper.readValue(bitmexExecution.getClOrdID(), OrderContext.class);
        } else {
            log.warn("Alien order cannot be processed {}", bitmexExecution.getOrderID());
            return;
        }

        Map<Integer, Long> imbalanceMap = tradingContext.getRecalculatedTradingContext().getImbalanceMap();

        if (bitmexExecution.getExecType() == ExecutionType.NEW) {
            if (bitmexExecution.getSide() == TradingSignal.LONG) {
                log.info("Long accepted {}, {},imbalance reset", bitmexExecution.getClOrdID(), bitmexExecution.getOrderQty());
                imbalanceMap.put(orderContext.getO(), 0L);
            } else {
                log.info("Short accepted {}, {}", bitmexExecution.getClOrdID(), bitmexExecution.getOrderQty());
            }

        } else if (bitmexExecution.getExecType() == ExecutionType.TRADE) {
            if (bitmexExecution.getOrdStatus() == OrderStatus.FILLED || bitmexExecution.getOrdStatus() == OrderStatus.PARTIALLY_FILLED) {

                if (bitmexExecution.getSide() == TradingSignal.LONG) {
                    // first order
                    log.info("Long opened {}, {}", bitmexExecution.getClOrdID(), bitmexExecution.getOrderQty());

                    // second order, should repeat the setup
                    long orderVolume = bitmexExecution.getSide() == TradingSignal.LONG ? bitmexExecution.getOrderQty() : -bitmexExecution.getOrderQty();
                    long oldVolume = imbalanceMap.get(orderContext.getO());
                    long newVolume = oldVolume + orderVolume;

                    imbalanceMap.put(orderContext.getO(), newVolume);

                    if (newVolume == bitmexExecution.getOrderQty()) {
                        log.info("Long volume reached the level to open short order {}, {}",
                            bitmexExecution.getClOrdID(), bitmexExecution.getOrderQty());

                        // open

                        Order<String> closeOrder = Order.buildMarketOrder(
                            tradingContext.getImmutableTradingContext().getTradeableInstrument(),
                            newVolume,
                            TradingSignal.SHORT,
                            CommonConsts.INVALID_PRICE,
                            CommonConsts.INVALID_PRICE
                        );

                        orderExecutionEngine.submit(closeOrder);
                    }

                } else {
                    log.info("Short opened {}, {}, finishing the cycle", bitmexExecution.getClOrdID(), bitmexExecution.getOrderQty());
                }
            }
        }

    }

    @Override
    public void onOrderResultCallback(TradingContext tradingContext, OrderResultContext<String> orderResultContext) {
        if (log.isDebugEnabled()) {
            log.debug("Order result callback {}", orderResultContext.toString());
        }
    }

    @Override
    public Collection<Order<String>> cancelAllPendingOrders() {
        Collection<Order<String>> pendingOrders = orderManagementProvider.allPendingOrders();
        pendingOrders.forEach(order -> orderManagementProvider.closeOrder(order.getOrderId(), 1L));

        return pendingOrders;
    }


}
