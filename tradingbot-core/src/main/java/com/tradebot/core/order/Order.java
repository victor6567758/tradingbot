package com.tradebot.core.order;

import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class Order<N> {

    private final TradeableInstrument instrument;
    private final long units;
    private final TradingSignal side;
    private final OrderType type;
    private final double price;
    private final double stopPrice;

    private final double takeProfit;
    private final double stopLoss;

    @Setter
    private N orderId;

    @Setter
    private N clientOrderId;

    @Setter
    private String text;

    public static <N> Order<N> buildMarketOrder(
        TradeableInstrument instrument,
        long units,
        TradingSignal side,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.MARKET)
            .price(0.0)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(0.0)
            .build();
    }

    public static <N> Order<N> buildLimitOrder(
        TradeableInstrument instrument,
        long units,
        TradingSignal side,
        double price,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.LIMIT)
            .price(price)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(0.0)
            .build();
    }

    public static <N> Order<N> buildStopMarketOrder(
        TradeableInstrument instrument,
        long units,
        TradingSignal side,
        double stopPrice,
        double takeProfit,
        double stopLoss
     ) {
        return Order.<N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.STOP_MARKET)
            .price(0.0)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(stopPrice)
            .build();
    }

    public static <N> Order<N> buildStopLimitOrder(
        TradeableInstrument instrument,
        long units,
        TradingSignal side,
        double stopPrice,
        double price,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.STOP_LIMIT)
            .price(price)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(stopPrice)
            .build();
    }

}
