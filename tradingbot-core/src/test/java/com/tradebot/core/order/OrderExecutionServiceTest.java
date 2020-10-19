
package com.tradebot.core.order;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;

public class OrderExecutionServiceTest<N> {

	@Test
	@SuppressWarnings("unchecked")
	public void placeOrderTest() {
		BlockingQueue<TradingDecision<N>> orderQueue = new LinkedBlockingQueue<TradingDecision<N>>();
		AccountInfoService<Long, N> accountInfoService = mock(AccountInfoService.class);
		OrderManagementProvider<Long, N, Long> orderManagementProvider = mock(OrderManagementProvider.class);
		BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
		PreOrderValidationService<Long, N, Long> preOrderValidationService = mock(PreOrderValidationService.class);
		CurrentPriceInfoProvider<N> currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
		OrderExecutionService<Long, N, Long> service = new OrderExecutionService<Long, N, Long>(orderQueue,
				accountInfoService, orderManagementProvider, baseTradingConfig, preOrderValidationService,
				currentPriceInfoProvider);
		service.init();
		TradeableInstrument<N> gbpaud = new TradeableInstrument<N>("GBP_AUD");
		TradingSignal signal = TradingSignal.SHORT;
		TradingDecision<N> tradingDecision1 = new TradingDecision<N>(gbpaud, signal, 1.855, 2.21);/*market order*/
		TradingDecision<N> tradingDecision2 = new TradingDecision<N>(gbpaud, signal, 1.855, 2.21, 2.12);/*limit order*/
		when(preOrderValidationService.checkInstrumentNotAlreadyTraded(gbpaud)).thenReturn(true);
		when(preOrderValidationService.checkLimitsForCcy(gbpaud, signal)).thenReturn(true);
		Collection<TradeableInstrument<N>> instruments = Lists.newArrayList();
		instruments.add(gbpaud);
		Map<TradeableInstrument<N>, Price<N>> priceMap = Maps.newHashMap();
		double bidPrice = 2.0557;
		double askPrice = 2.0562;
		priceMap.put(gbpaud, new Price<N>(gbpaud, bidPrice, askPrice, DateTime.now()));
		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(instruments))).thenReturn(priceMap);
		when(preOrderValidationService.isInSafeZone(TradingSignal.SHORT, bidPrice, gbpaud)).thenReturn(true);
		when(accountInfoService.findAccountsToTrade()).thenReturn(Lists.newArrayList(TradingTestConstants.ACCOUNT_ID_1));
		when(baseTradingConfig.getMaxAllowedQuantity()).thenReturn(100);
		orderQueue.offer(tradingDecision1);
		orderQueue.offer(tradingDecision2);
		/*this is a dummy trading decision payload, after whose consumption we know that our test case is tested*/
		orderQueue.offer(new TradingDecision<N>(gbpaud, TradingSignal.NONE));
		while (orderQueue.size() > 0) {
			try {
				Thread.sleep(2L);/*sleep instead of spinning*/
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		verify(orderManagementProvider, times(2)).placeOrder(any(Order.class), eq(TradingTestConstants.ACCOUNT_ID_1));
	}
}
