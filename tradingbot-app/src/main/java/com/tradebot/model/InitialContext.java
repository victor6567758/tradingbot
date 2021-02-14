package com.tradebot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class InitialContext {
    private final double x;
    private final double priceEnd;
    private final int linesNum;
    private final int orderPosUnits;
    private final String reportCurrency;
    private String reportExchangePair;
}
