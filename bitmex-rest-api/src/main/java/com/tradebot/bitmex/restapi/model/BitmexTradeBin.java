package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexTradeBin {
    private DateTime timestamp;
    private String symbol;
    private double open;
    private double high;
    private double low;
    private double close;
    private long trades;
    private long volume;
}
