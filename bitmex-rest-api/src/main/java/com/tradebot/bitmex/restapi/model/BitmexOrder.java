package com.tradebot.bitmex.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.order.OrderType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexOrder {

    private String orderID;

    private String symbol;

    private double orderQty;

    private double price;

    //Valid options: Buy, Sell.
    private TradingSignal side;

    // Valid options: Market, Limit, Stop, StopLimit, MarketIfTouched, LimitIfTouched, MarketWithLeftOverAsLimit, Pegged.
    private OrderType ordType;

    //Day, GoodTillCancel, ImmediateOrCancel, FillOrKill. Defaults to 'GoodTillCancel' for 'Limit', 'StopLimit', 'LimitIfTouched',
    // and 'MarketWithLeftOverAsLimit' orders.
    private String timeInForce;

    //Valid options: ParticipateDoNotInitiate, AllOrNone, MarkPrice, IndexPrice, LastPrice, Close, ReduceOnly, Fixed.
    // 'AllOrNone' instruction requires displayQty to be 0. 'MarkPrice', 'IndexPrice' or 'LastPrice' instruction valid for 'Stop', 'StopLimit', 'MarketIfTouched', and 'LimitIfTouched' orders.
    private String execInst;

    // New | Filled | PartiallyFilled | Canceled | Rejected
    private OrderStatus ordStatus;
}
