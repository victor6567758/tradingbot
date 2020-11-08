package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexExecution {

    private String execID;

    private String orderID;

    private String symbol;

    private double lastQty;

    private double lastPx;

    private double orderQty;

    //Valid options: Buy, Sell.
    private String side;

    // Valid options: Market, Limit, Stop, StopLimit, MarketIfTouched, LimitIfTouched, MarketWithLeftOverAsLimit, Pegged.
    private String ordType;

    private String execType;

    private String ordStatus;

    private String ordRejectReason;

    private double leavesQty;

    private double execCost;

    private double execCommision;

    private DateTime transactTime;

    private DateTime timestamp;


}
