package com.tradebot.core.order;

import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Order<M, N> {
    private final TradeableInstrument<M> instrument;
    private final long units;
    private final TradingSignal side;
    private final OrderType type;
    private final double takeProfit;
    private final double stopLoss;

    @Setter
    private N orderId;
    private final double price;

    /*
     * orderId not included in constructor because normally it is assigned by the platform only after order is placed successfully.
     */
    public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type, double price) {
        this(instrument, units, side, type, 0.0, 0.0, price);
    }

    public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type) {
        this(instrument, units, side, type, 0.0, 0.0);
    }

    public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type, double takeProfit,
            double stopLoss) {
        this(instrument, units, side, type, takeProfit, stopLoss, 0.0);
    }

    public Order(TradeableInstrument<M> instrument, long units, TradingSignal side, OrderType type, double takeProfit,
            double stopLoss, double price) {
        this.instrument = instrument;
        this.units = units;
        this.side = side;
        this.type = type;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.price = price;
    }

}
