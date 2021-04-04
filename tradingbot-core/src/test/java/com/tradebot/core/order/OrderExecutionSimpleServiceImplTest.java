
package com.tradebot.core.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.model.BaseTradingConfig;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.OrderExecutionServiceCallback;
import com.tradebot.core.model.TradingDecision;
import com.tradebot.core.model.TradingDecision.SrcDecision;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.utils.CommonConsts;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

@SuppressWarnings("unchecked")
public class OrderExecutionSimpleServiceImplTest {

    @RequiredArgsConstructor
    private static class OrderMatcher implements ArgumentMatcher<Order<Long>> {
        private final long orderId;

        @Override
        public boolean matches(Order<Long> argument) {
            return argument != null && argument.getClientOrderId() == orderId;
        }
    }

    private static final int ALLOWED_SUBMISSIONS = 20;
    private static final OperationResultContext<Long> ORDER_RESULT_CONTEXT = new OperationResultContext<>(1L, "GBP_AUD");

    private static final long ORDER1_CLIENT_ID = 1L;
    private static final long ORDER2_CLIENT_ID = 2L;

    private static final long ORDER1_ID = 1L;
    private static final long ORDER2_ID = 2L;


    private AccountInfoService<Long> accountInfoService = mock(AccountInfoService.class);
    private OrderManagementProvider<Long, Long> orderManagementProvider = mock(OrderManagementProvider.class);
    private BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
    private CurrentPriceInfoProvider currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
    private TradeableInstrument gbpaud = new TradeableInstrument("GBP_AUD", "GBP_AUD", 0.001, null, null, null, null, null);

    private final TradingDecision<Long> tradingDecision1 = TradingDecision.<Long>builder()
        .signal(TradingSignal.SHORT).instrument(gbpaud).tradeSource(SrcDecision.OTHER).limitPrice(1.855).stopPrice(2.21).units(1L)
        .takeProfitPrice(CommonConsts.INVALID_PRICE).stopLossPrice(CommonConsts.INVALID_PRICE)
        .context(ORDER1_CLIENT_ID)
        .build();

    private final TradingDecision<Long> tradingDecision2 = TradingDecision.<Long>builder()
        .signal(TradingSignal.SHORT).instrument(gbpaud).tradeSource(SrcDecision.OTHER).limitPrice(1.855).stopPrice(2.21).units(1L)
        .takeProfitPrice(2.12).stopLossPrice(CommonConsts.INVALID_PRICE)
        .context(ORDER2_CLIENT_ID)
        .build();

    @Before
    public void init() {

        Collection<TradeableInstrument> instruments = new ArrayList<>();
        instruments.add(gbpaud);
        Map<TradeableInstrument, Price> priceMap = new HashMap<>();
        double bidPrice = 2.0557;
        double askPrice = 2.0562;
        priceMap.put(gbpaud, new Price(gbpaud, bidPrice, askPrice, DateTime.now()));

        when(orderManagementProvider.placeOrder(argThat(new OrderMatcher(ORDER1_ID)), eq(TradingTestConstants.ACCOUNT_ID_1)))
            .thenReturn(new OperationResultContext<>(ORDER1_ID));

        when(orderManagementProvider.placeOrder(argThat(new OrderMatcher(ORDER2_ID)), eq(TradingTestConstants.ACCOUNT_ID_1)))
            .thenReturn(new OperationResultContext<>(ORDER2_ID));

        when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(instruments)))
            .thenReturn(priceMap);
        when(currentPriceInfoProvider
            .getCurrentPricesForInstrument(eq(instruments.iterator().next())))
            .thenReturn(priceMap.values().iterator().next());

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
            public void onOperationResult(OperationResultContext<?> orderResultContext) {

            }
        };

        OrderExecutionSimpleServiceImpl<Long, Long, Long> service = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountInfoService.findAccountsToTrade().iterator().next(),
            orderExecutionServiceCallback);

        List<Order<Long>> tradingOrderList1 = service.createOrderListFromDecision(tradingDecision1, id -> id);
        List<Order<Long>> tradingOrderList2 = service.createOrderListFromDecision(tradingDecision2, id -> id);

        Future<List<Order<Long>>> futureTask1 = service.submit(tradingOrderList1);
        Future<List<Order<Long>>> futureTask2 = service.submit(tradingOrderList2);

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
            public void onOperationResult(OperationResultContext<?> orderResultContext) {

            }
        };

        OrderExecutionSimpleServiceImpl<Long, Long, Long> service = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountInfoService.findAccountsToTrade().iterator().next(),
            orderExecutionServiceCallback);

        List<Order<Long>> tradingOrderList = service.createOrderListFromDecision(tradingDecision1, id -> id);

        for (int i = 0; i < ALLOWED_SUBMISSIONS; i++) {
            Future<List<Order<Long>>> future = service.submit(tradingOrderList);
            assertThat(future.get()).isNotEmpty();
        }

        Future<List<Order<Long>>> future = service.submit(tradingOrderList);
        assertThat(future.get()).isEmpty();

        service.shutdown();
    }

    @Test
    public void testOrderSubmitAfterDelay() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);
        List<Long> returnedOrders = new ArrayList<>();

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
            public void onOperationResult(OperationResultContext<?> orderResultContext) {
                long orderId = (Long)orderResultContext.getData();
                assertThat(orderResultContext.isResult()).isTrue();

                returnedOrders.add(orderId);
                latch.countDown();
            }
        };

        OrderExecutionSimpleServiceImpl<Long, Long, Long> service = new OrderExecutionSimpleServiceImpl<>(
            orderManagementProvider,
            () -> accountInfoService.findAccountsToTrade().iterator().next(),
            orderExecutionServiceCallback);

        List<Order<Long>> tradingOrderList1 = service.createOrderListFromDecision(tradingDecision1, id -> id);
        List<Order<Long>> tradingOrderList2 = service.createOrderListFromDecision(tradingDecision2, id -> id);

        Future<List<Order<Long>>> future1 = service.submit(tradingOrderList1, 5);
        Future<List<Order<Long>>> future2 = service.submit(tradingOrderList2);

        latch.await();
        assertThat(returnedOrders).containsExactly(ORDER2_ID, ORDER1_ID);
    }

}
