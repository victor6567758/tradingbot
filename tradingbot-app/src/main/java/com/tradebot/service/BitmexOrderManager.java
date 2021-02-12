package com.tradebot.service;

import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.bitmex.restapi.order.BitmexOrderManagementProvider;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.OrderExecutionServiceBase;
import com.tradebot.core.order.OrderExecutionSimpleServiceImpl;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.util.InitialContext;
import com.tradebot.util.TradingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BitmexOrderManager {

    private final OrderManagementProvider<String, Long> orderManagementProvider = new BitmexOrderManagementProvider();
    private OrderExecutionServiceBase<String, Long> orderExecutionEngine;

    public void initialize(long accountId) {

        orderExecutionEngine = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountId
        );
    }

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

    public void onOrderCallback(BitmexOrderEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order callback {}", event.getPayLoad().toString());
        }
    }

    public void onOrderExecutionCallback(BitmexExecutionEventPayload event) {
        if (log.isDebugEnabled()) {
            log.debug("Order execution callback {}", event.getPayLoad().toString());
        }

    }


}
