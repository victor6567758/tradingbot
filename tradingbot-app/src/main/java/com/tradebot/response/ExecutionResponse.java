package com.tradebot.response;

import com.tradebot.core.ExecutionType;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.order.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {

    private long dateTime;

    private TradingSignal side;

    private OrderType ordType;

    private ExecutionType execType;

    private OrderStatus ordStatus;

    double execPrice;

    private long lots;

    private String originalOrderId;
}
