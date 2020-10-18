package com.precioustech.fxtrading;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BaseTradingConfig {
    private double minReserveRatio;
    private double minAmountRequired;

    private int maxAllowedQuantity;
    private int maxAllowedNetContracts;
    private double max10yrWmaOffset;

}
