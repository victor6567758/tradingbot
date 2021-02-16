package com.tradebot.service;

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
import com.tradebot.core.order.OrderType;
import com.tradebot.model.TradingContext;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void onOrderExecutionCallback(TradingContext tradingContext, BitmexExecutionEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order execution callback {}", event.getPayLoad().toString());
        }

        BitmexExecution bitmexExecution = event.getPayLoad();
        Map<UUID, BigDecimal> imbalanceMap = tradingContext.getRecalculatedTradingContext().getImbalanceMap();
        UUID key = UUID.fromString(bitmexExecution.getOrderID());

        if (bitmexExecution.getExecType() == ExecutionType.NEW) {

        }
        else if (bitmexExecution.getExecType() == ExecutionType.TRADE) {

        }

        if (bitmexExecution.getOrdType() == OrderType.LIMIT) {

        }

        if (bitmexExecution.getOrdStatus() == OrderStatus.FILLED || bitmexExecution.getOrdStatus() == OrderStatus.PARTIALLY_FILLED) {

        }

        if (bitmexExecution.getSide() == TradingSignal.LONG) {
           // enter signal

        }


    }

    @Override
    public void onOrderResultCallback(TradingContext tradingContext, OrderResultContext<String> orderResultContext) {
        if (log.isDebugEnabled()) {
            log.debug("Order result callback {}", orderResultContext.toString());
        }
    }


}
