package com.tradebot.bitmex.restapi.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.OandaTestUtils;
import com.tradebot.core.account.Account;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;


public class BitmexAccountDataProviderServiceTest {

    private BitmexAccountDataProviderService createSpyAndCommonStuff(String fname,
        BitmexAccountDataProviderService service) throws Exception {
        BitmexAccountDataProviderService spy = spy(service);

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);

        OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);

        return spy;
    }

    // TODO: this test logs "java.io.IOException: Stream Closed" because of same
    // FileInputStream reread once closed
    @Test
    public void allAccountsTest() throws Exception {
        final BitmexAccountDataProviderService service = new BitmexAccountDataProviderService(
            BitmexTestConstants.url, BitmexTestConstants.userName, BitmexTestConstants.accessToken);
        assertEquals("https://api-fxtrade.oanda.com/v1/accounts?username=testTrader",
            service.getAllAccountsUrl());
        BitmexAccountDataProviderService spy = createSpyAndCommonStuff(
            "src/test/resources/accountsAll.txt", service);
        spy.getLatestAccountInfo();
        verify(spy, times(1)).getSingleAccountUrl(1898212L);
        verify(spy, times(1)).getSingleAccountUrl(2093221L);
    }

    @Test
    public void accountIdTest() throws Exception {
        final BitmexAccountDataProviderService service = new BitmexAccountDataProviderService(
            BitmexTestConstants.url, BitmexTestConstants.userName, BitmexTestConstants.accessToken);
        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456",
            service.getSingleAccountUrl(
                BitmexTestConstants.accountId));

        BitmexAccountDataProviderService spy = createSpyAndCommonStuff(
            "src/test/resources/account123456.txt", service);
        Account<Long> accInfo = spy.getLatestAccountInfo(BitmexTestConstants.accountId);
        assertNotNull(accInfo);
        assertEquals("CHF", accInfo.getCurrency());
        assertEquals(0.05, accInfo.getMarginRate(), BitmexTestConstants.precision);
        assertEquals(-897.1, accInfo.getUnrealisedPnl(), BitmexTestConstants.precision);
    }
}
