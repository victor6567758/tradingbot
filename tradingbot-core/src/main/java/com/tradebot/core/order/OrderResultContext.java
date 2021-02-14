package com.tradebot.core.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@RequiredArgsConstructor
public class OrderResultContext<N> {

    private final N orderId;
    private final boolean result;
}
