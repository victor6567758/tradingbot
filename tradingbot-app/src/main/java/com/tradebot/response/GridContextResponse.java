package com.tradebot.response;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GridContextResponse {
    private final List<Double> mesh;
    private final double oneLotPrice;
    private final String symbol;
}
