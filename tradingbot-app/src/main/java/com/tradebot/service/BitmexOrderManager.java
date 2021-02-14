package com.tradebot.service;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.model.TradingContext;

public interface BitmexOrderManager {

    void initialize(long accountId, BitmexAccountConfiguration bitmexAccountConfiguration);

    void submitOrder(Order<String> order);

    void startOrderEvolution(TradingContext tradingContext);

    void onCandleCallback(CandleStick candleStick, CacheCandlestick cacheCandlestick, TradingContext tradingContext);

    void onOrderCallback(TradingContext tradingContext, BitmexOrderEventPayload event);

    void onOrderExecutionCallback(TradingContext tradingContext, BitmexExecutionEventPayload event);

    void onOrderResultCallback(TradingContext tradingContext, OrderResultContext<String> orderResultContext);

}
