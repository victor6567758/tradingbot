
package com.tradebot.core.order;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.OrderExecutionServiceCallback;
import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.instrument.TradeableInstrument;

@SuppressWarnings("unchecked")
public class OrderInfoServiceTest<N> {

	@Test
	public void netPositionCountForCurrencyTest() {
		OrderManagementProvider<Long, Long> orderManagementProvider = mock(OrderManagementProvider.class);

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
		OrderInfoService<Long, Long> service = new OrderInfoService<>(orderManagementProvider, orderExecutionServiceCallback);

		Collection<Order<Long>> orders = createOrders();
		OperationResultContext<Collection<Order<Long>>> orderResultsList = new OperationResultContext<>(orders);
		OperationResultContext<Order<Long>> orderResult = new OperationResultContext<>(orders.iterator().next());

		when(orderManagementProvider.allPendingOrders()).thenReturn(orderResultsList);
		when(orderManagementProvider.pendingOrdersForAccount(TradingTestConstants.ACCOUNT_ID_1))
			.thenReturn(orderResultsList);
		when(orderManagementProvider.pendingOrdersForAccount(TradingTestConstants.ACCOUNT_ID_2))
			.thenReturn(orderResultsList);

		TradeableInstrument usdchf = new TradeableInstrument("USD_CHF", "USD_CHF", 0.001, null, null, null, null, null);
		when(orderManagementProvider.pendingOrdersForInstrument(eq(usdchf)))
			.thenReturn(orderResultsList);

		when(orderManagementProvider.pendingOrderForAccount(TradingTestConstants.ORDER_ID, TradingTestConstants.ACCOUNT_ID_1))
			.thenReturn(orderResult);

		assertEquals(0, service.findNetPositionCountForCurrency("EUR"));
		assertEquals(2, service.findNetPositionCountForCurrency("JPY"));
		assertEquals(-1, service.findNetPositionCountForCurrency("GBP"));
		assertEquals(0, service.findNetPositionCountForCurrency("XAU"));

		/*test other wrapper methods as well. may have to change if caching is introduced*/
		service.pendingOrderForAccount(TradingTestConstants.ORDER_ID, TradingTestConstants.ACCOUNT_ID_1);
		verify(orderManagementProvider, times(1)).pendingOrderForAccount(TradingTestConstants.ORDER_ID,
				TradingTestConstants.ACCOUNT_ID_1);

		service.pendingOrdersForAccount(TradingTestConstants.ACCOUNT_ID_2);
		verify(orderManagementProvider, times(1)).pendingOrdersForAccount(TradingTestConstants.ACCOUNT_ID_2);


		service.pendingOrdersForInstrument(usdchf);
		verify(orderManagementProvider, times(1)).pendingOrdersForInstrument(usdchf);
	}

	private Collection<Order<Long>> createOrders() {
		Collection<Order<Long>> orders = Lists.newArrayList();

		Order<Long> order1 = mock(Order.class);
		TradeableInstrument eurjpy = new TradeableInstrument("EUR_JPY","EUR_JPY", 0.001, null, null, null, null, null);
		when(order1.getInstrument()).thenReturn(eurjpy);
		when(order1.getSide()).thenReturn(TradingSignal.SHORT);
		when(order1.getOrderId()).thenReturn(TradingTestConstants.ORDER_ID);
		orders.add(order1);

		Order<Long> order2 = mock(Order.class);
		TradeableInstrument eurusd = new TradeableInstrument("EUR_USD","EUR_USD", 0.001, null, null, null, null, null);
		when(order2.getInstrument()).thenReturn(eurusd);
		when(order2.getSide()).thenReturn(TradingSignal.LONG);
		orders.add(order2);

		Order<Long> order3 = mock(Order.class);
		TradeableInstrument gbpjpy = new TradeableInstrument("GBP_JPY","GBP_JPY", 0.001, null, null, null, null, null);
		when(order3.getInstrument()).thenReturn(gbpjpy);
		when(order3.getSide()).thenReturn(TradingSignal.SHORT);
		orders.add(order3);

		return orders;
	}
}
