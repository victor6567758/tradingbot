
package com.tradebot.core.order;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.tradebot.core.model.BaseTradingConfig;
import com.tradebot.core.utils.TradingConstants;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.core.marketdata.historic.MovingAverageCalculationService;
import com.tradebot.core.trade.TradeInfoService;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;

//TODO: if the launch configuration does not exist then have generics below will suppress the availability of run as junit.
//the trick is to move <M,N,K> to the test case and run the test. once the launch is created then move it back to the class.
@SuppressWarnings("unchecked")
public class PreOrderValidationServiceTest<N, K> {

    @Test
    public void priceInSafeZoneTest() {
        MovingAverageCalculationService movingAvgCalcService = mock(MovingAverageCalculationService.class);
        BaseTradingConfig baseTradingCfg = mock(BaseTradingConfig.class);
        PreOrderValidationService<N, K> service = new PreOrderValidationService<>(null, movingAvgCalcService,
            baseTradingCfg, null);
        when(baseTradingCfg.getMax10yrWmaOffset()).thenReturn(0.1);
        TradeableInstrument eurusd = new TradeableInstrument("EUR_USD", "EUR_USD", 0.001, null, null, null, null, null);
        when(
            movingAvgCalcService.calculateWMA(eq(eurusd), eq(PreOrderValidationService.FIVE_YRS_IN_MTHS),
                eq(CandleStickGranularity.M))).thenReturn(1.22);

        assertTrue(service.isInSafeZone(TradingSignal.LONG, 1.3, eurusd));
        assertTrue(service.isInSafeZone(TradingSignal.SHORT, 1.11, eurusd));
        assertFalse(service.isInSafeZone(TradingSignal.LONG, 1.36, eurusd));
        assertFalse(service.isInSafeZone(TradingSignal.SHORT, 1.05, eurusd));
    }

    @Test
    public void instrumentNotAlreadyTradedTest() {
        TradeInfoService<N, Long> tradeInfoService = mock(TradeInfoService.class);
        OrderInfoService<N, Long> orderInfoService = mock(OrderInfoService.class);
        PreOrderValidationService<N, Long> service = new PreOrderValidationService<>(tradeInfoService,
            null, null, orderInfoService);

        Collection<Long> accountIds = Lists.newArrayList();
        accountIds.add(TradingTestConstants.ACCOUNT_ID_1);
        accountIds.add(TradingTestConstants.ACCOUNT_ID_2);

        TradeableInstrument gbpusd = new TradeableInstrument("GBP_USD", "GBP_USD", 0.001, null, null, null, null, null);
        TradeableInstrument nzdjpy = new TradeableInstrument("NZD_JPY", "NZD_JPY", 0.001, null, null, null, null, null);
        TradeableInstrument audchf = new TradeableInstrument("AUD_CHF", "AUD_CHF", 0.001, null, null, null, null, null);
        when(tradeInfoService.findAllAccountsWithInstrumentTrades(gbpusd)).thenReturn(accountIds);
        assertFalse(service.checkInstrumentNotAlreadyTraded(gbpusd));

        Collection<Long> emptyCollectionIds = Collections.emptyList();
        when(tradeInfoService.findAllAccountsWithInstrumentTrades(nzdjpy)).thenReturn(emptyCollectionIds);

        Collection<Order<N>> pendingOrders = Lists.newArrayList();
        when(orderInfoService.pendingOrdersForInstrument(nzdjpy)).thenReturn(pendingOrders);
        pendingOrders.add(mock(Order.class));
        pendingOrders.add(mock(Order.class));
        assertFalse(service.checkInstrumentNotAlreadyTraded(nzdjpy));
        Collection<Order<N>> emptyCollectionOrders = Collections.emptyList();
        when(tradeInfoService.findAllAccountsWithInstrumentTrades(audchf)).thenReturn(emptyCollectionIds);
        when(orderInfoService.pendingOrdersForInstrument(audchf)).thenReturn(emptyCollectionOrders);
        assertTrue(service.checkInstrumentNotAlreadyTraded(audchf));
    }

    @Test
    public void limitsForCcyTest() {
        final String AUD = "AUD";
        final String NZD = "NZD";
        final String CAD = "CAD";
        final String CHF = "CHF";

        TradeInfoService<N, Long> tradeInfoService = mock(TradeInfoService.class);
        OrderInfoService<N, Long> orderInfoService = mock(OrderInfoService.class);
        BaseTradingConfig baseTradingCfg = mock(BaseTradingConfig.class);
        PreOrderValidationService<N, Long> service = new PreOrderValidationService<>(tradeInfoService,
            null, baseTradingCfg, orderInfoService);
        TradeableInstrument audnzd = new TradeableInstrument(
            AUD + TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE + NZD, AUD + TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE + NZD,
            0.001, null, null, null, null, null
        );
        when(baseTradingCfg.getMaxAllowedNetContracts()).thenReturn(4);
        when(tradeInfoService.findNetPositionCountForCurrency(AUD)).thenReturn(4);
        when(tradeInfoService.findNetPositionCountForCurrency(NZD)).thenReturn(3);
        when(orderInfoService.findNetPositionCountForCurrency(AUD)).thenReturn(-1);
        when(orderInfoService.findNetPositionCountForCurrency(NZD)).thenReturn(1);
        assertFalse(service.checkLimitsForCcy(audnzd, TradingSignal.SHORT));
        TradeableInstrument cadchf = new TradeableInstrument(
            CAD + TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE + CHF, CAD + TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE + CHF,
            0.001, null, null, null, null, null);
        when(tradeInfoService.findNetPositionCountForCurrency(CAD)).thenReturn(5);
        when(tradeInfoService.findNetPositionCountForCurrency(CHF)).thenReturn(1);
        when(orderInfoService.findNetPositionCountForCurrency(CAD)).thenReturn(1);
        when(orderInfoService.findNetPositionCountForCurrency(CHF)).thenReturn(1);
        assertTrue(service.checkLimitsForCcy(cadchf, TradingSignal.SHORT));
    }
}
