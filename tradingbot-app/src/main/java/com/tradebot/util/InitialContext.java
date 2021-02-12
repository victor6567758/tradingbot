package com.tradebot.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InitialContext {
    private final double x;
    private final double priceEnd;
    private final int linesNum;
    private final int orderPosUnits;
}
