package com.tradebot.model;

import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.order.Order;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TradingContext {

    private final TradeableInstrument tradeableInstrument;
    private final Map<BigDecimal, TradingDecision> tradingGrid = new TreeMap<>();
    private final Map<String, Order<String>> currentOrders = new HashMap<>();
    private double oneLotPrice;
    private CandleStick candleStick;
    private boolean tradeEnabled = true;
    private boolean started = false;
    private BitmexOrderQuotas bitmexOrderQuotas;
}
