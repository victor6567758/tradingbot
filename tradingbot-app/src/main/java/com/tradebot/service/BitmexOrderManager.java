package com.tradebot.service;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.OrderExecutionServiceBase;
import com.tradebot.core.order.OrderExecutionServiceCallback;
import com.tradebot.core.order.OrderExecutionSimpleServiceImpl;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.model.InitialContext;
import com.tradebot.model.TradingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BitmexOrderManager implements OrderExecutionServiceCallback {

    private final OrderManagementProvider<String, Long> orderManagementProvider;
    private OrderExecutionServiceBase<String, Long> orderExecutionEngine;
    private BitmexAccountConfiguration bitmexAccountConfiguration;

    public void initialize(long accountId, BitmexAccountConfiguration bitmexAccountConfiguration) {

        orderExecutionEngine = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountId,
            this);

        this.bitmexAccountConfiguration = bitmexAccountConfiguration;
    }

    // TradingContext is locked
    public void startOrderEvolution(InitialContext initialContext, TradingContext tradingContext) {
        tradingContext.getTradingGrid().values().forEach(decision -> {
            orderExecutionEngine.submit(decision);
        });
    }

    public void onCandleCallback(CandleStick candleStick, CacheCandlestick cacheCandlestick,
        InitialContext initialContext, TradingContext tradingContext) {

        if (log.isDebugEnabled()) {
            log.debug("Candle callback {}", candleStick.toString());
        }

        if (tradingContext.isTradeEnabled()) {

        }

    }

    public void onOrderCallback(TradingContext tradingContext, BitmexOrderEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order callback {}", event.getPayLoad().toString());
        }
    }

    public void onOrderExecutionCallback(TradingContext tradingContext, BitmexExecutionEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order execution callback {}", event.getPayLoad().toString());
        }

    }


    @Override
    public void fired() {

    }

    @Override
    public boolean ifTradeAllowed() {
        return false;
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public void onOrderResult(OrderResultContext orderResultContext) {
        if (log.isDebugEnabled()) {
            log.debug("Order result context {}", orderResultContext.toString());
        }
    }
}
