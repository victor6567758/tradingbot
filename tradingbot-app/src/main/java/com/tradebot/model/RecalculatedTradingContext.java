package com.tradebot.model;

import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.marketdata.historic.CandleStick;
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
    private double profitPlus;
    private final Map<BigDecimal, TradingDecision> openTradingDecisions = new TreeMap<>();
    private final Map<Integer, Long> imbalanceMap = new HashMap<>();
    private boolean tradeEnabled = true;
    private boolean ordersProcessingStarted;
    private boolean oneTimeInitialized;
    private BitmexOrderQuotas bitmexOrderQuotas;
}
