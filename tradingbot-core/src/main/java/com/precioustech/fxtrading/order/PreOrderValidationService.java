package com.precioustech.fxtrading.order;

import com.precioustech.fxtrading.BaseTradingConfig;
import com.precioustech.fxtrading.TradingSignal;
import com.precioustech.fxtrading.instrument.TradeableInstrument;
import com.precioustech.fxtrading.marketdata.historic.CandleStickGranularity;
import com.precioustech.fxtrading.marketdata.historic.MovingAverageCalculationService;
import com.precioustech.fxtrading.trade.TradeInfoService;
import com.precioustech.fxtrading.utils.TradingUtils;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreOrderValidationService<M, N, K> {

    static final int FIVE_YRS_IN_MTHS = 60;

    private final TradeInfoService<M, N, K> tradeInfoService;
    private final MovingAverageCalculationService<N> movingAverageCalculationService;
    private final BaseTradingConfig baseTradingConfig;
    private final OrderInfoService<M, N, K> orderInfoService;

    public PreOrderValidationService(TradeInfoService<M, N, K> tradeInfoService,
        MovingAverageCalculationService<N> movingAverageCalculationService,
        BaseTradingConfig baseTradingConfig,
        OrderInfoService<M, N, K> orderInfoService) {
        this.tradeInfoService = tradeInfoService;
        this.movingAverageCalculationService = movingAverageCalculationService;
        this.baseTradingConfig = baseTradingConfig;
        this.orderInfoService = orderInfoService;
    }

    public boolean isInSafeZone(TradingSignal signal, double price,
        TradeableInstrument<N> instrument) {
        // check 10yr wma and make sure we are 10% on either side
        double wma10yr = this.movingAverageCalculationService
            .calculateWMA(instrument, FIVE_YRS_IN_MTHS,
                CandleStickGranularity.M);
        final double max10yrWmaOffset = baseTradingConfig.getMax10yrWmaOffset();
        double minPrice = (1.0 - max10yrWmaOffset) * wma10yr;
        double maxPrice = (1.0 + max10yrWmaOffset) * wma10yr;
        if ((signal == TradingSignal.SHORT && price > minPrice) || (signal == TradingSignal.LONG
            && price < maxPrice)) {
            return true;
        } else {
            log.info(
                "Rejecting {}  {} because price {} is 10pct on either side of wma 10yr price of {}",
                signal, instrument.getInstrument(), price, wma10yr);
            return false;
        }
    }

    public boolean checkInstrumentNotAlreadyTraded(TradeableInstrument<N> instrument) {
        Collection<K> accIds = this.tradeInfoService
            .findAllAccountsWithInstrumentTrades(instrument);
        if (accIds.size() > 0) {
            log.warn("Rejecting trade with instrument {} as trade already exists",
                instrument.getInstrument());
            return false;
        } else {
            Collection<Order<N, M>> pendingOrders = this.orderInfoService
                .pendingOrdersForInstrument(instrument);
            if (!pendingOrders.isEmpty()) {
                log.warn("Pending order with instrument {} already exists",
                    instrument.getInstrument());
                return false;
            }
            return true;
        }
    }

    public boolean checkLimitsForCcy(TradeableInstrument<N> instrument, TradingSignal signal) {
        String[] currencies = TradingUtils.splitInstrumentPair(instrument.getInstrument());
        for (String currency : currencies) {
            int positionCount = this.tradeInfoService.findNetPositionCountForCurrency(currency)
                + this.orderInfoService.findNetPositionCountForCurrency(currency);
            int sign = TradingUtils.getSign(instrument.getInstrument(), signal, currency);
            int newPositionCount = positionCount + sign;
            if (Math.abs(newPositionCount) > this.baseTradingConfig.getMaxAllowedNetContracts()
                && Integer.signum(sign) == Integer.signum(positionCount)) {
                log.warn("Cannot place trade {} because max limit exceeded. max allowed={} and future net positions={} for currency {} if trade executed",
                    instrument.getInstrument(),
                    baseTradingConfig.getMaxAllowedNetContracts(),
                    newPositionCount,
                    currency);
                return false;
            }
        }
        return true;
    }

}
