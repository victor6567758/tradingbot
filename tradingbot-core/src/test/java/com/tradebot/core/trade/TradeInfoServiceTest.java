
package com.tradebot.core.trade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.instrument.InstrumentService;
import java.util.Collection;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;

public class TradeInfoServiceTest {

	@Test
	public void netPositionCountForCurrencyTest() {
		TradeInfoService<Long, Long> service = createService();
		int jpyCt = service.findNetPositionCountForCurrency("JPY");
		assertEquals(0, jpyCt);

		int nzdCt = service.findNetPositionCountForCurrency("NZD");
		assertEquals(1, nzdCt);

		int audCt = service.findNetPositionCountForCurrency("AUD");
		assertEquals(-1, audCt);
	}

	@Test
	public void allTradesTest() {
		TradeInfoService<Long, Long> service = createService();
		Collection<Trade<Long, Long>> allTrades = service.getAllTrades();
		assertEquals(7, allTrades.size());
	}

	@Test
	public void allTradesForAccountAndInstrumentTest() {
		TradeInfoService<Long, Long> service = createService();
		Collection<Trade<Long, Long>> trades = service.getTradesForAccountAndInstrument(
				TradingTestConstants.ACCOUNT_ID_1, new TradeableInstrument("USD_JPY", "USD_JPY", 0.001, null, null, null, null, null));
		assertFalse(trades.isEmpty());
		trades = service.getTradesForAccountAndInstrument(TradingTestConstants.ACCOUNT_ID_1,
				new TradeableInstrument("USD_CHF", "USD_CHF", 0.001, null, null, null, null, null));
		assertTrue(trades.isEmpty());
	}

	@Test
	public void tradeExistsTest() {
		TradeInfoService<Long, Long> service = createService();
		assertTrue(service.isTradeExistsForInstrument(new TradeableInstrument("AUD_USD", "AUD_USD", 0.001, null, null, null, null, null)));
		assertFalse(service.isTradeExistsForInstrument(new TradeableInstrument("AUD_CHF","AUD_CHF", 0.001, null, null, null, null, null)));
	}

	@Test
	public void tradesForAccountTest() {
		TradeInfoService<Long, Long> service = createService();
		Collection<Trade<Long, Long>> allTradesAcc1 = service
				.getTradesForAccount(TradingTestConstants.ACCOUNT_ID_1);
		assertEquals(3, allTradesAcc1.size());
		// assertEquals(allTradesAcc1.contains(new
		// TradeableInstrument("EUR_USD")));
		Collection<Trade<Long, Long>> allTradesAcc2 = service
				.getTradesForAccount(TradingTestConstants.ACCOUNT_ID_2);
		assertEquals(4, allTradesAcc2.size());
		// assertEquals(allTradesAcc1.contains(new
		// TradeableInstrument("GBP_NZD")));

	}

	@Test
	public void accountsForInstrumentsTest() {
		TradeInfoService<Long, Long> service = createService();
		Collection<Long> accountIds = service
				.findAllAccountsWithInstrumentTrades(new TradeableInstrument("EUR_USD","EUR_USD", 0.001, null, null, null, null, null));
		assertEquals(2, accountIds.size());
		accountIds = service.findAllAccountsWithInstrumentTrades(new TradeableInstrument("EUR_JPY","EUR_JPY", 0.001, null, null, null, null, null));
		assertEquals(1, accountIds.size());
	}

	@SuppressWarnings("unchecked")
	private TradeInfoService<Long, Long> createService() {
		TradeManagementProvider<Long, Long> tradeManagementProvider = mock(TradeManagementProvider.class);
		AccountInfoService<Long> accInfoService = mock(AccountInfoService.class);


		TradeInfoService<Long, Long> service = new TradeInfoService<>(tradeManagementProvider, accInfoService);
		service.init();
		Account<Long> account1 = mock(Account.class);
		Account<Long> account2 = mock(Account.class);
		when(account1.getAccountId()).thenReturn(TradingTestConstants.ACCOUNT_ID_1);
		when(account2.getAccountId()).thenReturn(TradingTestConstants.ACCOUNT_ID_2);
		when(accInfoService.getAllAccounts()).thenReturn(Lists.newArrayList(account1, account2));
		when(tradeManagementProvider.getTradesForAccount(TradingTestConstants.ACCOUNT_ID_1))
				.thenReturn(createSampleTrades1());
		when(tradeManagementProvider.getTradesForAccount(TradingTestConstants.ACCOUNT_ID_2))
				.thenReturn(createSampleTrades2());
		service.init();
		return service;
	}

	private Collection<Trade<Long, Long>> createSampleTrades1() {
		Collection<Trade<Long, Long>> trades = Lists.newArrayList();
		trades.add(
				new Trade(2001L, 10, TradingSignal.LONG, new TradeableInstrument("GBP_USD", "GBP_USD", 0.001, null, null, null, null, null),
						DateTime.now(), 0.0, 1.5365, 0.0, TradingTestConstants.ACCOUNT_ID_1));
		trades.add(
				new Trade(2003L, 10, TradingSignal.LONG, new TradeableInstrument("USD_JPY","USD_JPY", 0.001, null, null, null, null, null),
						DateTime.now(), 0.0, 120.15, 0.0, TradingTestConstants.ACCOUNT_ID_1));
		trades.add(new Trade(2005L, 10, TradingSignal.SHORT,
				new TradeableInstrument("EUR_USD","EUR_USD", 0.001, null, null, null, null, null), DateTime.now(), 0.0, 1.2429, 0.0,
				TradingTestConstants.ACCOUNT_ID_1));
		return trades;
	}

	private Collection<Trade<Long, Long>> createSampleTrades2() {
		Collection<Trade<Long, Long>> trades = Lists.newArrayList();

		trades.add(new Trade(2002L, 10, TradingSignal.SHORT,
				new TradeableInstrument("EUR_JPY","EUR_JPY", 0.001, null, null, null, null, null), DateTime.now(), 0.0, 135.55, 0.0,
				TradingTestConstants.ACCOUNT_ID_2));
		trades.add(new Trade(2004L, 10, TradingSignal.SHORT,
				new TradeableInstrument("GBP_NZD","GBP_NZD", 0.001, null, null, null, null, null), DateTime.now(), 0.0, 2.39, 0.0,
				TradingTestConstants.ACCOUNT_ID_2));
		trades.add(new Trade(2006L, 10, TradingSignal.SHORT,
				new TradeableInstrument("AUD_USD","AUD_USD", 0.001, null, null, null, null, null), DateTime.now(), 0.0, 0.8123, 0.0,
				TradingTestConstants.ACCOUNT_ID_2));
		trades.add(
				new Trade(2007L, 10, TradingSignal.LONG, new TradeableInstrument("EUR_USD", "EUR_USD", 0.001, null, null, null, null, null),
						DateTime.now(), 0.0, 1.2515, 0.0, TradingTestConstants.ACCOUNT_ID_2));
		return trades;
	}
}
