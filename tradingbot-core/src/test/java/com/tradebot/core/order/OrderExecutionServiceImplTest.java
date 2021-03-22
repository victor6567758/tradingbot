
package com.tradebot.core.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.core.model.BaseTradingConfig;
import com.tradebot.core.model.OrderExecutionServiceCallback;
import com.tradebot.core.model.TradingDecision;
import com.tradebot.core.model.TradingDecision.SrcDecison;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.utils.CommonConsts;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class OrderExecutionServiceImplTest<N> {

    private static final int ALLOWED_SUBMISSIONS = 20;
    private static final OperationResultContext<Long> ORDER_RESULT_CONTEXT = new OperationResultContext<>(1L, "GBP_AUD");

    private final AccountInfoService<Long> accountInfoService = mock(AccountInfoService.class);
    private final OrderManagementProvider<Long, Long> orderManagementProvider = mock(OrderManagementProvider.class);
    private final BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
    private final PreOrderValidationService<Long, Long> preOrderValidationService = mock(PreOrderValidationService.class);
    private final CurrentPriceInfoProvider currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
    private final TradeableInstrument gbpaud = new TradeableInstrument("GBP_AUD", "GBP_AUD", 0.001, null, null, null, null, null);
    private final TradingSignal signal = TradingSignal.SHORT;

    private final TradingDecision<Object> tradingDecision1 = TradingDecision.builder()
        .signal(signal).instrument(gbpaud).tradeSource(SrcDecison.OTHER).limitPrice(1.855).stopPrice(2.21).units(1L)
        .takeProfitPrice(CommonConsts.INVALID_PRICE).stopLossPrice(CommonConsts.INVALID_PRICE)
        .build();

    private final TradingDecision<Object> tradingDecision2 = TradingDecision.builder()
        .signal(signal).instrument(gbpaud).tradeSource(SrcDecison.OTHER).limitPrice(1.855).stopPrice(2.21).units(1L)
        .takeProfitPrice(2.12).stopLossPrice(CommonConsts.INVALID_PRICE)
        .build();

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

        when(orderManagementProvider.placeOrder(any(Order.class), eq(TradingTestConstants.ACCOUNT_ID_1)))
            .thenReturn(ORDER_RESULT_CONTEXT);

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

        OrderExecutionServiceCallback orderExecutionServiceCallback = new OrderExecutionServiceCallback() {

            @Override
            public void fired() {
            }

            @Override
            public boolean ifTradeAllowed() {
                return true;
            }

            @Override
            public String getReason() {
                return null;
            }

            @Override
            public void onOrderResult(OperationResultContext<?> orderResultContext) {

            }
        };

        OrderExecutionServiceImpl<Long,Long, Object> service = new OrderExecutionServiceImpl<>(
            accountInfoService,
            orderManagementProvider,
            baseTradingConfig,
            preOrderValidationService,
            currentPriceInfoProvider,
            orderExecutionServiceCallback);


        Future<List<Order<Long>>> futureTask1 = service.submit(tradingDecision1);
        Future<List<Order<Long>>> futureTask2 = service.submit(tradingDecision2);

        assertThat(futureTask1.get()).isNotEmpty();
        assertThat(futureTask2.get()).isNotEmpty();

        verify(orderManagementProvider, times(2))
            .placeOrder(any(Order.class), eq(TradingTestConstants.ACCOUNT_ID_1));

        service.shutdown();
    }

    @Test
    public void testOrderSubmissionIsInterrupted() throws ExecutionException, InterruptedException {

        OrderExecutionServiceCallback orderExecutionServiceCallback = new OrderExecutionServiceCallback() {

            private final AtomicInteger counter = new AtomicInteger(ALLOWED_SUBMISSIONS);

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

            @Override
            public void onOrderResult(OperationResultContext orderResultContext) {

            }
        };

        OrderExecutionServiceImpl<Long, Long, Object> service = new OrderExecutionServiceImpl<>(
            accountInfoService,
            orderManagementProvider,
            baseTradingConfig,
            preOrderValidationService,
            currentPriceInfoProvider,
            orderExecutionServiceCallback);

        for (int i = 0; i < ALLOWED_SUBMISSIONS; i++) {
            Future<List<Order<Long>>> future = service.submit(tradingDecision1);
            assertThat(future.get()).isNotEmpty();
        }

        Future<List<Order<Long>>> future = service.submit(tradingDecision1);
        assertThat(future.get()).isEmpty();

        service.shutdown();
    }

}
