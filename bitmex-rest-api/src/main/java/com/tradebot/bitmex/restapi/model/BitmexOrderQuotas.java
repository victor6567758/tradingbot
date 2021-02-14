package com.tradebot.bitmex.restapi.model;

import com.tradebot.core.order.OrderResultContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class BitmexOrderQuotas extends OrderResultContext<String> {

    private int xRatelimitLimit = -1;
    private int xRatelimitRemaining = -1;
    private int xRatelimitReset = -1;
    private int xRatelimitRemaining1s = -1;

    public BitmexOrderQuotas(String orderId, boolean result) {
        super(orderId, result);
    }
}
