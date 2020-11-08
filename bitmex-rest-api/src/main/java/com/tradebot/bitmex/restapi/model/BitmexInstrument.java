package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexInstrument {

    private String symbol;
    private double indicativeFundingRate;
    private double fundingRate;
    private DateTime fundingTimestamp;
}
