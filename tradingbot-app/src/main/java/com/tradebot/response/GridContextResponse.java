package com.tradebot.response;

import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class GridContextResponse {

    private Set<Double> mesh;
    private String symbol;
    private double oneLotPrice;
}
