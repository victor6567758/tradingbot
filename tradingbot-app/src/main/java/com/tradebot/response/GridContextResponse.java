package com.tradebot.response;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;


@Data
@NoArgsConstructor
public class GridContextResponse {

    private List<Pair<Double, Integer>> mesh;
    private String symbol;
    private CandleResponse candleResponse;
    private Map<Integer, List<ExecutionResponse>> executionResponseList;
    private LimitResponse limitResponse;
}
