package com.tradebot.model;

import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.Order;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RecalculatedTradingContext {

    private CandleStick candleStick;
    private final Map<BigDecimal, TradingDecision> tradingGrid = new TreeMap<>();
    private final Map<String, Order<String>> currentOrders = new HashMap<>();
    private boolean tradeEnabled = true;
    private boolean ordersProcessingStarted;
    private boolean oneTimeInitialized;
    private BitmexOrderQuotas bitmexOrderQuotas;
}
