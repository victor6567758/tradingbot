package com.tradebot.core.order;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrderResultContext<N> {

    private final N data;
    private final boolean result;
    private final String symbol;
    private final String message;

    public OrderResultContext(N data, String symbol, String message) {
        this.data = data;
        this.result = false;
        this.symbol = symbol;
        this.message = message;
    }

    public OrderResultContext(N data, String symbol) {
        this.data = data;
        this.result = true;
        this.symbol = symbol;
        this.message = "OK";
    }

    public OrderResultContext(N data) {
        this.data = data;
        this.result = true;
        this.symbol = null;
        this.message = "OK";
    }
}
