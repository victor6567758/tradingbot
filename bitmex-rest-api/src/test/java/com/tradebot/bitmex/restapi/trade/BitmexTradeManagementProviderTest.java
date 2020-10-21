package com.tradebot.bitmex.restapi.trade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.OandaTestUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.trade.Trade;
import java.util.Collection;
import java.util.Iterator;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

public class BitmexTradeManagementProviderTest {

    @Test
    public void modifyTradeTest() throws Exception {
        BitmexTradeManagementProvider service = new BitmexTradeManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken);
        final long tradeId = BitmexTestConstants.tradeId;
        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades/1800805337", service
            .getTradeForAccountUrl(tradeId, BitmexTestConstants.accountId));
        BitmexTradeManagementProvider spy = doMockStuff(
            "src/test/resources/tradesForAccount123456.txt", service);

        final double stopLoss = 150.0;
        final double takeProfit = 110.0;
        spy.modifyTrade(BitmexTestConstants.accountId, tradeId, stopLoss, takeProfit);
        verify(spy, times(1))
            .createPatchCommand(BitmexTestConstants.accountId, tradeId, stopLoss, takeProfit);

    }

    @Test
    public void closeTradeTest() throws Exception {
        BitmexTradeManagementProvider service = new BitmexTradeManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken);
        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades/1800805337", service
            .getTradeForAccountUrl(BitmexTestConstants.tradeId, BitmexTestConstants.accountId));
        BitmexTradeManagementProvider spy = doMockStuff(
            "src/test/resources/tradesForAccount123456.txt", service);

        boolean success = spy
            .closeTrade(BitmexTestConstants.tradeId, BitmexTestConstants.accountId);
        assertTrue(success);
        verify(spy.getHttpClient(), times(1)).execute(any(HttpDelete.class));
    }

    @Test
    public void givenTradeForAccTest() throws Exception {
        BitmexTradeManagementProvider service = new BitmexTradeManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken);

        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades/1800805337", service
            .getTradeForAccountUrl(BitmexTestConstants.tradeId, BitmexTestConstants.accountId));

        BitmexTradeManagementProvider spy = doMockStuff(
            "src/test/resources/trade1800805337ForAccount123456.txt",
            service);
        Trade<Long, String, Long> trade = spy.getTradeForAccount(BitmexTestConstants.tradeId,
            BitmexTestConstants.accountId);
        assertEquals(TradingSignal.SHORT, trade.getSide());
        assertEquals(3000L, trade.getUnits());
        assertEquals(120.521, trade.getExecutionPrice(), BitmexTestConstants.precision);
        assertEquals(105.521, trade.getTakeProfitPrice(), BitmexTestConstants.precision);
        assertEquals(121.521, trade.getStopLoss(), BitmexTestConstants.precision);
    }

    private BitmexTradeManagementProvider doMockStuff(String fname,
        BitmexTradeManagementProvider service)
        throws Exception {
        BitmexTradeManagementProvider spy = Mockito.spy(service);
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);
        OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);
        return spy;
    }

    @Test
    public void allTradesForAccTest() throws Exception {
        BitmexTradeManagementProvider service = new BitmexTradeManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken);

        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/trades", service
            .getTradesInfoUrl(BitmexTestConstants.accountId));

        BitmexTradeManagementProvider spy = doMockStuff(
            "src/test/resources/tradesForAccount123456.txt", service);

        Collection<Trade<Long, String, Long>> trades = spy
            .getTradesForAccount(BitmexTestConstants.accountId);
        assertEquals(2, trades.size());

        Iterator<Trade<Long, String, Long>> itr = trades.iterator();
        Trade<Long, String, Long> trade1 = itr.next();
        Trade<Long, String, Long> trade2 = itr.next();

        assertEquals(TradingSignal.SHORT, trade1.getSide());
        assertEquals(3000L, trade1.getUnits());
        assertEquals(120.521, trade1.getExecutionPrice(), BitmexTestConstants.precision);
        assertEquals(105.521, trade1.getTakeProfitPrice(), BitmexTestConstants.precision);
        assertEquals(121.521, trade1.getStopLoss(), BitmexTestConstants.precision);

        assertEquals(TradingSignal.LONG, trade2.getSide());
        assertEquals(3000L, trade2.getUnits());
        assertEquals(1.0098, trade2.getExecutionPrice(), BitmexTestConstants.precision);
        assertEquals(1.15979, trade2.getTakeProfitPrice(), BitmexTestConstants.precision);
        assertEquals(0.9854, trade2.getStopLoss(), BitmexTestConstants.precision);

    }
}
