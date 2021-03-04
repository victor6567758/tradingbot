package com.tradebot.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
public class GridContextResponse {

    private List<MeshEntry> mesh;
    private String symbol;
    private CandleResponse candleResponse;
    private Map<Integer, List<ExecutionResponse>> executionResponseList;
    private LimitResponse limitResponse;
}
