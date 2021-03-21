package com.tradebot.service;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.events.payload.BitmexOrderEventPayload;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.Order;
import com.tradebot.model.TradingContext;
import java.util.Collection;

public interface BitmexOrderManager {

    void initialize(long accountId, BitmexAccountConfiguration bitmexAccountConfiguration);

    void submitOrder(Order<String> order);

    boolean startOrderEvolution(TradingContext tradingContext);

    void onCandleCallback(CandleStick candleStick, CacheCandlestick cacheCandlestick, TradingContext tradingContext);

    void onOrderCallback(TradingContext tradingContext, BitmexOrderEventPayload event);

    void onOrderExecutionCallback(TradingContext tradingContext, BitmexExecutionEventPayload event);

    Collection<Order<String>> cancelAllPendingOrders();

}
