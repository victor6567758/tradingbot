
package com.tradebot.core.order;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.instrument.TradeableInstrument;

@SuppressWarnings("unchecked")
public class OrderInfoServiceTest<N> {

	@Test
	public void netPositionCountForCurrencyTest() {
		final OrderManagementProvider<Long, Long> orderManagementProvider = mock(OrderManagementProvider.class);
		OrderInfoService<Long, Long> service = new OrderInfoService<Long, Long>(orderManagementProvider);
		Collection<Order<Long>> orders = createOrders();
		when(orderManagementProvider.allPendingOrders()).thenReturn(orders);
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

		TradeableInstrument usdchf = new TradeableInstrument("USD_CHF", "USD_CHF");
		service.pendingOrdersForInstrument(usdchf);
		verify(orderManagementProvider, times(1)).pendingOrdersForInstrument(usdchf);
	}

	private Collection<Order<Long>> createOrders() {
		Collection<Order<Long>> orders = Lists.newArrayList();

		Order<Long> order1 = mock(Order.class);
		TradeableInstrument eurjpy = new TradeableInstrument("EUR_JPY","EUR_JPY");
		when(order1.getInstrument()).thenReturn(eurjpy);
		when(order1.getSide()).thenReturn(TradingSignal.SHORT);
		orders.add(order1);

		Order<Long> order2 = mock(Order.class);
		TradeableInstrument eurusd = new TradeableInstrument("EUR_USD","EUR_USD");
		when(order2.getInstrument()).thenReturn(eurusd);
		when(order2.getSide()).thenReturn(TradingSignal.LONG);
		orders.add(order2);

		Order<Long> order3 = mock(Order.class);
		TradeableInstrument gbpjpy = new TradeableInstrument("GBP_JPY","GBP_JPY");
		when(order3.getInstrument()).thenReturn(gbpjpy);
		when(order3.getSide()).thenReturn(TradingSignal.SHORT);
		orders.add(order3);

		return orders;
	}
}
