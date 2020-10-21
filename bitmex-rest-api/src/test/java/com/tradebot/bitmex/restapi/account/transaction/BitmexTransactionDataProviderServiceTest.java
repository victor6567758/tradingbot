package com.tradebot.bitmex.restapi.account.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.OandaTestUtils;
import com.tradebot.bitmex.restapi.events.AccountEvents;
import com.tradebot.bitmex.restapi.events.OrderEvents;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.transaction.Transaction;
import java.math.BigInteger;
import java.util.List;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

public class BitmexTransactionDataProviderServiceTest {

    @Test
    public void fetchTransctionTest() throws Exception {
        final BitmexTransactionDataProviderService service = new BitmexTransactionDataProviderService(
            BitmexTestConstants.url, BitmexTestConstants.accessToken);
        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/transactions/1800806000",
            service.getSingleAccountTransactionUrl(BitmexTestConstants.transactionId,
                BitmexTestConstants.accountId));
        BitmexTransactionDataProviderService spy = createSpyAndCommonStuff(
            "src/test/resources/transaction123456.txt",
            service);
        Transaction<Long, Long, String> transaction = spy.getTransaction(
            BitmexTestConstants.transactionId, BitmexTestConstants.accountId);
        assertNotNull(transaction);
        assertEquals("EUR_CHF", transaction.getInstrument().getInstrument());
        assertEquals(TradingSignal.SHORT, transaction.getSide());
        assertEquals(1.10642, transaction.getPrice(), BitmexTestConstants.precision);
        assertEquals(0.2538, transaction.getPnl(), BitmexTestConstants.precision);
        assertEquals(new Long(200), transaction.getUnits());
        assertEquals(TradeEvents.TRADE_CLOSE, transaction.getTransactionType());
        assertEquals(BitmexTestConstants.accountId, transaction.getAccountId());
        assertEquals(0.0, transaction.getInterest(), BitmexTestConstants.precision);
    }

    @Test
    public void historicTransactionsTest() throws Exception {
        final long minId = 175000000L;
        final BitmexTransactionDataProviderService service = new BitmexTransactionDataProviderService(
            BitmexTestConstants.url, BitmexTestConstants.accessToken);
        assertEquals(
            "https://api-fxtrade.oanda.com/v1/accounts/123456/transactions?minId=" + (minId + 1)
                + "&count=500",
            service.getAccountMinTransactionUrl(minId, BitmexTestConstants.accountId));
        BitmexTransactionDataProviderService spy = createSpyAndCommonStuff(
            "src/test/resources/historicTransactions.txt",
            service);
        List<Transaction<Long, Long, String>> allTransactions = spy
            .getTransactionsGreaterThanId(minId, BitmexTestConstants.accountId);
        assertFalse(allTransactions.isEmpty());
        assertEquals(9, allTransactions.size());

        // general not null checks common to all
        for (Transaction<Long, Long, String> transaction : allTransactions) {
            assertNotNull(transaction.getTransactionId());
            assertNotNull(transaction.getAccountId());
            assertNotNull(transaction.getTransactionTime());
            assertNotNull(transaction.getTransactionType());
            assertNotNull(transaction.getInstrument());
            assertEquals(BitmexTestConstants.accountId, transaction.getAccountId());
        }

        // TRADE_CLOSE
        Transaction<Long, Long, String> transaction = allTransactions.get(0);
        assertEquals(TradeEvents.TRADE_CLOSE, transaction.getTransactionType());
        assertEquals("EUR_USD", transaction.getInstrument().getInstrument());
        assertEquals(new Long(2), transaction.getUnits());
        assertEquals(TradingSignal.SHORT, transaction.getSide());
        assertEquals(1.25918, transaction.getPrice(), BitmexTestConstants.precision);
        assertEquals(0.0119, transaction.getPnl(), BitmexTestConstants.precision);
        assertEquals(new Long(176403879), transaction.getLinkedTransactionId());

        // TRADE_UPDATE
        transaction = allTransactions.get(1);
        assertEquals(TradeEvents.TRADE_UPDATE, transaction.getTransactionType());
        assertEquals("USD_SGD", transaction.getInstrument().getInstrument());
        assertEquals(new Long(3000), transaction.getUnits());
        assertEquals(new Long(1782311741), transaction.getLinkedTransactionId());

        // TAKE_PROFIT_FILLED
        transaction = allTransactions.get(2);
        assertEquals(TradeEvents.TAKE_PROFIT_FILLED, transaction.getTransactionType());
        assertEquals("USD_CHF", transaction.getInstrument().getInstrument());
        assertEquals(new Long(3000), transaction.getUnits());
        assertEquals(new Long(1782379135), transaction.getLinkedTransactionId());
        assertEquals(1.00877, transaction.getPrice(), BitmexTestConstants.precision);
        assertEquals(3.48, transaction.getPnl(), BitmexTestConstants.precision);
        assertEquals(TradingSignal.SHORT, transaction.getSide());
        assertEquals(0.0002, transaction.getInterest(), BitmexTestConstants.precision);

        // STOP_LOSS_FILLED
        transaction = allTransactions.get(3);
        assertEquals(TradeEvents.STOP_LOSS_FILLED, transaction.getTransactionType());
        assertEquals("USD_SGD", transaction.getInstrument().getInstrument());
        assertEquals(new Long(3000), transaction.getUnits());
        assertEquals(new Long(1782311741), transaction.getLinkedTransactionId());
        assertEquals(1.39101, transaction.getPrice(), BitmexTestConstants.precision);
        assertEquals(3.3039, transaction.getPnl(), BitmexTestConstants.precision);
        assertEquals(TradingSignal.SHORT, transaction.getSide());
        assertEquals(-0.0123, transaction.getInterest(), BitmexTestConstants.precision);

        // TRAILING_STOP_FILLED
        transaction = allTransactions.get(4);
        assertEquals(TradeEvents.TRAILING_STOP_FILLED, transaction.getTransactionType());
        assertEquals("EUR_USD", transaction.getInstrument().getInstrument());
        assertEquals(new Long(10), transaction.getUnits());
        assertEquals(new Long(175739352), transaction.getLinkedTransactionId());
        assertEquals(1.38137, transaction.getPrice(), BitmexTestConstants.precision);
        assertEquals(-0.0009, transaction.getPnl(), BitmexTestConstants.precision);
        assertEquals(TradingSignal.SHORT, transaction.getSide());
        assertEquals(0.0, transaction.getInterest(), BitmexTestConstants.precision);

        // LIMIT_ORDER_CREATE
        transaction = allTransactions.get(6);
        assertEquals(OrderEvents.LIMIT_ORDER_CREATE, transaction.getTransactionType());
        assertEquals("EUR_USD", transaction.getInstrument().getInstrument());
        assertEquals(new Long(2), transaction.getUnits());
        assertEquals(BigInteger.ZERO.longValue(), transaction.getLinkedTransactionId().longValue());
        assertEquals(1, transaction.getPrice(), BitmexTestConstants.precision);
        assertEquals(TradingSignal.LONG, transaction.getSide());

        // DAILY_INTEREST
        transaction = allTransactions.get(8);
        assertEquals(AccountEvents.DAILY_INTEREST, transaction.getTransactionType());
        assertEquals("AUD_USD", transaction.getInstrument().getInstrument());
        assertNull(transaction.getUnits());
        assertNull(transaction.getSide());

    }

    private BitmexTransactionDataProviderService createSpyAndCommonStuff(String fname,
        BitmexTransactionDataProviderService service) throws Exception {
        BitmexTransactionDataProviderService spy = Mockito.spy(service);

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);

        OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);

        return spy;
    }

}
