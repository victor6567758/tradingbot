package com.tradebot.request;

import lombok.Data;

@Data
public class LimitOrderRequest {

    private String symbol;
    private long lots;
    private double limitPrice;
    private String tradingSignal;
}
