package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexQuote {
    private String symbol;
    private double bidPrice;
    private double askPrice;
    private int bidSize;
    private int askSize;
    private DateTime timestamp;
}
