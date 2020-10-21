
package com.tradebot.core.order;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.Test;

public class OrderExecutionServiceTest<N> {

    @Test
    @SuppressWarnings("unchecked")
    public void placeOrderTest() {
        AccountInfoService<Long, N> accountInfoService = mock(AccountInfoService.class);
        OrderManagementProvider<Long, N, Long> orderManagementProvider = mock(
            OrderManagementProvider.class);
        BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);

        PreOrderValidationService<Long, N, Long> preOrderValidationService = mock(
            PreOrderValidationService.class);
        CurrentPriceInfoProvider<N> currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
        OrderExecutionService<Long, N, Long> service = new OrderExecutionService<>(
            accountInfoService,
            orderManagementProvider,
            baseTradingConfig,
            preOrderValidationService,
            currentPriceInfoProvider);

        TradeableInstrument<N> gbpaud = new TradeableInstrument<>("GBP_AUD");
        TradingSignal signal = TradingSignal.SHORT;

        /*market order*/
        TradingDecision<N> tradingDecision1 = new TradingDecision<>(gbpaud, signal, 1.855, 2.21);
        /*limit order*/
        TradingDecision<N> tradingDecision2 = new TradingDecision<>(gbpaud, signal, 1.855, 2.21,
            2.12);

        when(preOrderValidationService.checkInstrumentNotAlreadyTraded(gbpaud)).thenReturn(true);
        when(preOrderValidationService.checkLimitsForCcy(gbpaud, signal)).thenReturn(true);
        Collection<TradeableInstrument<N>> instruments = new ArrayList<>();
        instruments.add(gbpaud);
        Map<TradeableInstrument<N>, Price<N>> priceMap = new HashMap<>();
        double bidPrice = 2.0557;
        double askPrice = 2.0562;
        priceMap.put(gbpaud, new Price<N>(gbpaud, bidPrice, askPrice, DateTime.now()));

        when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(instruments)))
            .thenReturn(priceMap);
        when(currentPriceInfoProvider
            .getCurrentPricesForInstrument(eq(instruments.iterator().next())))
            .thenReturn(priceMap.values().iterator().next());
        when(preOrderValidationService.isInSafeZone(TradingSignal.SHORT, bidPrice, gbpaud))
            .thenReturn(true);
        when(accountInfoService.findAccountsToTrade())
            .thenReturn(Collections.singletonList(TradingTestConstants.ACCOUNT_ID_1));
        when(accountInfoService.findAccountToTrade())
            .thenReturn(Optional.of(TradingTestConstants.ACCOUNT_ID_1));
        when(baseTradingConfig.getMaxAllowedQuantity()).thenReturn(100);

        service.submit(tradingDecision1);
        service.submit(tradingDecision2);

        /*this is a dummy trading decision payload, after whose consumption we know that our test case is tested*/
        service.submit(new TradingDecision<>(gbpaud, TradingSignal.NONE));
        verify(orderManagementProvider, times(2))
            .placeOrder(any(Order.class), eq(TradingTestConstants.ACCOUNT_ID_1));

        service.shutdown();
    }
}
