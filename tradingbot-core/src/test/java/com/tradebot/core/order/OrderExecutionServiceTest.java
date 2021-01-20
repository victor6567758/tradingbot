
package com.tradebot.core.order;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.checkerframework.checker.units.qual.A;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class OrderExecutionServiceTest<N> {

    private static final int ALLOWED_SUBMISSIONS = 20;

    private final AccountInfoService<Long> accountInfoService = mock(AccountInfoService.class);
    private final OrderManagementProvider<Long, Long> orderManagementProvider = mock(OrderManagementProvider.class);
    private final BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
    private final PreOrderValidationService<Long, Long> preOrderValidationService = mock(PreOrderValidationService.class);
    private final CurrentPriceInfoProvider currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
    private final TradeableInstrument gbpaud = new TradeableInstrument("GBP_AUD", "GBP_AUD");
    private final TradingSignal signal = TradingSignal.SHORT;
    private final TradingDecision tradingDecision1 = new TradingDecision(gbpaud, signal, 1.855, 2.21);
    private final TradingDecision tradingDecision2 = new TradingDecision(gbpaud, signal, 1.855, 2.21, 2.12);

    @Before
    public void init() {
        when(preOrderValidationService.checkInstrumentNotAlreadyTraded(gbpaud)).thenReturn(true);
        when(preOrderValidationService.checkLimitsForCcy(gbpaud, signal)).thenReturn(true);

        Collection<TradeableInstrument> instruments = new ArrayList<>();
        instruments.add(gbpaud);
        Map<TradeableInstrument, Price> priceMap = new HashMap<>();
        double bidPrice = 2.0557;
        double askPrice = 2.0562;
        priceMap.put(gbpaud, new Price(gbpaud, bidPrice, askPrice, DateTime.now()));

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
    }

    @Test
    public void testPlaceOrders() throws ExecutionException, InterruptedException {

        OrderExecutionServiceContext orderExecutionServiceContext = new OrderExecutionServiceContext() {

            @Override
            public boolean ifTradeAllowed() {
                return true;
            }

            @Override
            public String getReason() {
                return null;
            }
        };

        OrderExecutionService<Long, Long> service = new OrderExecutionService(
            accountInfoService,
            orderManagementProvider,
            baseTradingConfig,
            preOrderValidationService,
            currentPriceInfoProvider,
            orderExecutionServiceContext);





        Future<Boolean> futureTask1 = service.submit(tradingDecision1);
        Future<Boolean> futureTask2 = service.submit(tradingDecision2);

        assertThat(futureTask1.get()).isTrue();
        assertThat(futureTask2.get()).isTrue();

        verify(orderManagementProvider, times(2))
            .placeOrder(any(Order.class), eq(TradingTestConstants.ACCOUNT_ID_1));

        service.shutdown();
    }

    @Test
    public void testOrderSubmissionIsInterrupted() throws ExecutionException, InterruptedException {

        OrderExecutionServiceContext orderExecutionServiceContext = new OrderExecutionServiceContext() {

            private AtomicInteger counter = new AtomicInteger(ALLOWED_SUBMISSIONS);

            @Override
            public void fired() {
            }

            @Override
            public boolean ifTradeAllowed() {
                return counter.getAndDecrement() > 0;
            }

            @Override
            public String getReason() {
                return null;
            }
        };

        OrderExecutionService<Long, Long> service = new OrderExecutionService(
            accountInfoService,
            orderManagementProvider,
            baseTradingConfig,
            preOrderValidationService,
            currentPriceInfoProvider,
            orderExecutionServiceContext);

        for (int i = 0; i < ALLOWED_SUBMISSIONS; i++) {
            Future<Boolean> future = service.submit(tradingDecision1);
            assertThat(future.get()).isTrue();
        }

        Future<Boolean> future = service.submit(tradingDecision1);
        assertThat(future.get()).isFalse();

        service.shutdown();
    }

}
