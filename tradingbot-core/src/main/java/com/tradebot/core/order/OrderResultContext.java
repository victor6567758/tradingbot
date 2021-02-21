package com.tradebot.core.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class OrderResultContext<N> {

    private final N orderId;
    private final boolean result;
    private final String symbol;
    private final String message;
}
