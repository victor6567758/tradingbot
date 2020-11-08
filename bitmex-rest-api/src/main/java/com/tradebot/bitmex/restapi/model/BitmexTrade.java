package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradebot.core.TradingSignal;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexTrade {

    private String symbol;
    private TradingSignal side;
    private double price;
    private double size;
    private String timestamp;
}
