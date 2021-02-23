package com.tradebot.response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;


@Data
@NoArgsConstructor
public class GridContextResponse {

    private Set<Pair<Integer, Double>> mesh;
    private String symbol;
    private CandleResponse candleResponse;
    private Map<Integer, List<ExecutionResponse>> executionResponseList;
}
