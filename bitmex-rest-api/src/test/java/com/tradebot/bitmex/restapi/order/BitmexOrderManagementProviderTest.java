package com.tradebot.bitmex.restapi.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.BitmexTestUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderType;
import java.util.Collection;
import java.util.Iterator;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

public class BitmexOrderManagementProviderTest {

    @Test
    @SuppressWarnings("unchecked")
    public void allOrders() throws Exception {

        AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
        Account<Long> account = mock(Account.class);
        when(accountDataProvider.getLatestAccountsInfo()).thenReturn(Lists.newArrayList(account));
        when(account.getAccountId()).thenReturn(BitmexTestConstants.accountId);
        BitmexOrderManagementProvider service = new BitmexOrderManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken, accountDataProvider);
        BitmexOrderManagementProvider spy = doMockStuff("src/test/resources/allOrders.txt",
            service);
        Collection<Order<String, Long>> pendingOrders = spy.allPendingOrders();
        assertEquals(2, pendingOrders.size());
        Iterator<Order<String, Long>> iterator = pendingOrders.iterator();
        Order<String, Long> order1 = iterator.next();
        Order<String, Long> order2 = iterator.next();

        assertEquals("USD_CAD", order1.getInstrument().getInstrument());
        assertEquals(TradingSignal.LONG, order1.getSide());
        assertEquals(OrderType.LIMIT, order1.getType());
        assertEquals(1.3, order1.getPrice(), BitmexTestConstants.precision);
        assertEquals(1.2, order1.getStopLoss(), BitmexTestConstants.precision);
        assertEquals(1.31, order1.getTakeProfit(), BitmexTestConstants.precision);
        assertEquals(100l, order1.getUnits());

        assertEquals("EUR_USD", order2.getInstrument().getInstrument());
        assertEquals(TradingSignal.LONG, order2.getSide());
        assertEquals(OrderType.LIMIT, order2.getType());
        assertEquals(1.115, order2.getPrice(), BitmexTestConstants.precision);
        assertEquals(150l, order2.getUnits(), BitmexTestConstants.precision);
        assertEquals(0.0, order2.getStopLoss(), BitmexTestConstants.precision);
        assertEquals(0.0, order2.getTakeProfit(), BitmexTestConstants.precision);
    }

    @Test
    public void orderForAccount() throws Exception {
        BitmexOrderManagementProvider service = new BitmexOrderManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken, null);
        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/orders/1001",
            service.orderForAccountUrl(
                BitmexTestConstants.accountId, BitmexTestConstants.orderId));
        BitmexOrderManagementProvider spy = doMockStuff(
            "src/test/resources/orderForAccount123456.txt", service);
        Order<String, Long> order = spy
            .pendingOrderForAccount(BitmexTestConstants.orderId, BitmexTestConstants.accountId);
        assertNotNull(order);
        assertEquals("USD_JPY", order.getInstrument().getInstrument());
        assertEquals(TradingSignal.SHORT, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(122.15, order.getPrice(), BitmexTestConstants.precision);
        assertEquals(125l, order.getUnits());
        assertEquals(125.00, order.getStopLoss(), BitmexTestConstants.precision);
        assertEquals(119.25, order.getTakeProfit(), BitmexTestConstants.precision);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void modifyOrderTest() throws Exception {
        BitmexOrderManagementProvider service = new BitmexOrderManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken, null);
        BitmexOrderManagementProvider spy = doMockStuff(
            "src/test/resources/orderForAccount123456.txt", service);
        Order<String, Long> order = mock(Order.class);
        when(order.getTakeProfit()).thenReturn(119.45);
        when(order.getStopLoss()).thenReturn(124.75);
        when(order.getUnits()).thenReturn(1000l);
        when(order.getPrice()).thenReturn(122.0);
        when(order.getOrderId()).thenReturn(BitmexTestConstants.orderId);
        spy.modifyOrder(order, BitmexTestConstants.accountId);
        verify(spy, times(1)).createPatchCommand(order, BitmexTestConstants.accountId);
        verify(order, times(1)).getOrderId();
        verify(order, times(1)).getTakeProfit();
        verify(order, times(1)).getStopLoss();
        verify(order, times(1)).getPrice();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createOrderTest() throws Exception {
        BitmexOrderManagementProvider service = new BitmexOrderManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken, null);
        TradeableInstrument<String> eurjpy = new TradeableInstrument<String>("EUR_JPY");

        BitmexOrderManagementProvider spy = doMockStuff("src/test/resources/newOrder.txt", service);
        Order<String, Long> orderMarket = mock(Order.class);
        when(orderMarket.getInstrument()).thenReturn(eurjpy);
        when(orderMarket.getSide()).thenReturn(TradingSignal.SHORT);
        when(orderMarket.getType()).thenReturn(OrderType.MARKET);
        when(orderMarket.getUnits()).thenReturn(150l);
        when(orderMarket.getTakeProfit()).thenReturn(132.65);
        when(orderMarket.getStopLoss()).thenReturn(136.00);
        // when(order.getPrice()).thenReturn(133.75);
        Long orderId = spy.placeOrder(orderMarket, BitmexTestConstants.accountId);
        assertNotNull(orderId);
        verify(spy, times(1)).createPostCommand(orderMarket, BitmexTestConstants.accountId);
        verify(orderMarket, times(1)).getInstrument();
        verify(orderMarket, times(3)).getType();
        verify(orderMarket, times(1)).getTakeProfit();
        verify(orderMarket, times(1)).getStopLoss();
        // verify(order, times(2)).getPrice();
        verify(orderMarket, times(1)).getUnits();
        verify(orderMarket, times(1)).getSide();

        spy = doMockStuff("src/test/resources/newOrderLimit.txt", service);
        Order<String, Long> orderLimit = mock(Order.class);
        TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
        when(orderLimit.getInstrument()).thenReturn(eurusd);
        when(orderLimit.getSide()).thenReturn(TradingSignal.SHORT);
        when(orderLimit.getType()).thenReturn(OrderType.LIMIT);
        when(orderLimit.getUnits()).thenReturn(10l);
        when(orderLimit.getTakeProfit()).thenReturn(1.09);
        when(orderLimit.getStopLoss()).thenReturn(0.0);
        when(orderLimit.getPrice()).thenReturn(1.10);

        orderId = spy.placeOrder(orderLimit, BitmexTestConstants.accountId);
        assertNotNull(orderId);
        verify(spy, times(1)).createPostCommand(orderLimit, BitmexTestConstants.accountId);
        verify(orderLimit, times(1)).getInstrument();
        verify(orderLimit, times(3)).getType();
        verify(orderLimit, times(1)).getTakeProfit();
        verify(orderLimit, times(1)).getStopLoss();
        verify(orderLimit, times(2)).getPrice();
        verify(orderLimit, times(1)).getUnits();
        verify(orderLimit, times(1)).getSide();
    }

    @Test
    public void closeOrderTest() throws Exception {
        BitmexOrderManagementProvider service = new BitmexOrderManagementProvider(
            BitmexTestConstants.url,
            BitmexTestConstants.accessToken, null);

        BitmexOrderManagementProvider spy = doMockStuff(
            "src/test/resources/orderForAccount123456.txt", service);

        boolean success = spy
            .closeOrder(BitmexTestConstants.orderId, BitmexTestConstants.accountId);
        assertTrue(success);
        verify(spy.getHttpClient(), times(1)).execute(any(HttpDelete.class));
    }

    private BitmexOrderManagementProvider doMockStuff(String fname,
        BitmexOrderManagementProvider service)
        throws Exception {
        BitmexOrderManagementProvider spy = Mockito.spy(service);
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);
        BitmexTestUtils.mockHttpInteraction(fname, mockHttpClient);
        return spy;
    }
}
