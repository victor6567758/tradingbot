package com.tradebot.bitmex.restapi.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {
    NEW("New"),
    FILLED("Filled"),
    PARTIALLY_FILLED("PartiallyFilled"),
    CANCELLED("Canceled"),
    REJECTED("Rejected");

    private final String statusText;
}
