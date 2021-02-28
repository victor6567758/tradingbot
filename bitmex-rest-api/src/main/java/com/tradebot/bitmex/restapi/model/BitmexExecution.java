package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradebot.core.ExecutionType;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.order.OrderType;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexExecution {

    private String execID;

    private String orderID;

    private String clOrdID;

    private String symbol;

    private long lastQty;

    private double lastPx;

    private double price;

    private long orderQty;

    //Valid options: Buy, Sell.
    private TradingSignal side;

    // Valid options: Market, Limit, Stop, StopLimit, MarketIfTouched, LimitIfTouched, MarketWithLeftOverAsLimit, Pegged.
    private OrderType ordType;

    private ExecutionType execType;

    // New | Filled | PartiallyFilled | Canceled | Rejected
    private OrderStatus ordStatus;

    private String ordRejectReason;

    private double leavesQty;

    private double execCost;

    private double execCommision;

    private DateTime transactTime;

    private DateTime timestamp;


}
