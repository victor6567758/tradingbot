package com.tradebot.model;

import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.marketdata.historic.CandleStick;
import java.util.HashMap;
import java.util.List;
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

    // internal order id -> Trading decision
    private final Map<Integer, TradingDecision> openTradingDecisions = new TreeMap<>();
    private final Map<Integer, Long> imbalanceMap = new HashMap<>();
    private final Map<Integer, List<BitmexExecution>> executionChains = new HashMap<>();
    private boolean tradeEnabled = true;
    private boolean ordersProcessingStarted;
    private boolean oneTimeInitialized;
    private BitmexOrderQuotas bitmexOrderQuotas;
}
