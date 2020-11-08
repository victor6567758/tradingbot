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
public class Order<M, N> {

    private final TradeableInstrument<M> instrument;
    private final long units;
    private final TradingSignal side;
    private final OrderType type;
    private final double takeProfit;
    private final double stopLoss;
    private final double price;
    private final double stopPrice;

    @Setter
    private N orderId;

    public static <M, N> Order<M, N> buildMarketOrder(
        TradeableInstrument<M> instrument,
        long units,
        TradingSignal side,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<M, N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.MARKET)
            .price(0.0)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(0.0).build();
    }

    public static <M, N> Order<M, N> buildLimitOrder(
        TradeableInstrument<M> instrument,
        long units,
        TradingSignal side,
        double price,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<M, N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.LIMIT)
            .price(price)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(0.0).build();
    }

    public static <M, N> Order<M, N> buildStopMarketOrder(
        TradeableInstrument<M> instrument,
        long units,
        TradingSignal side,
        double stopPrice,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<M, N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.STOP_MARKET)
            .price(0.0)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(stopPrice).build();
    }

    public static <M, N> Order<M, N> buildStopLimitOrder(
        TradeableInstrument<M> instrument,
        long units,
        TradingSignal side,
        double stopPrice,
        double price,
        double takeProfit,
        double stopLoss
    ) {
        return Order.<M, N>builder()
            .instrument(instrument)
            .units(units)
            .side(side)
            .type(OrderType.STOP_LIMIT)
            .price(price)
            .takeProfit(takeProfit)
            .stopLoss(stopLoss)
            .stopPrice(stopPrice).build();
    }

}
