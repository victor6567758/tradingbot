package com.tradebot.response;

import com.tradebot.core.model.ExecutionType;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.order.OrderType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionResponse {

    private long dateTime;

    private TradingSignal side;

    private OrderType ordType;

    private ExecutionType execType;

    private OrderStatus ordStatus;

    private double lastPx;

    private double price;

    private double stopPx;

    private long lots;

    private String originalOrderId;
}
