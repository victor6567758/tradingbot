package com.tradebot.request;

import lombok.Data;

@Data
public class MarketOrderRequest {

    private String symbol;
    private long lots;
    private String tradingSignal;
    private String text;
}
