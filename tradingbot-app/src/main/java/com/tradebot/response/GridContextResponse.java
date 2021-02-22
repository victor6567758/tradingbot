package com.tradebot.response;

import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class GridContextResponse {

    private Set<Double> mesh;
    private String symbol;
    private CandleResponse candleResponse;
}
