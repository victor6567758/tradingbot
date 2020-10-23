package com.tradebot.bitmex.restapi.marketdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.BitmexTestUtils;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;


public class BitmexCurrentPriceInfoProviderTest {

    @Test
    public void currentPricesTest() throws Exception {
        BitmexCurrentPriceInfoProvider service = new BitmexCurrentPriceInfoProvider(
            BitmexTestConstants.url,
                BitmexTestConstants.accessToken);

        BitmexCurrentPriceInfoProvider spy = spy(service);

        TradeableInstrument<String> gbpusd = new TradeableInstrument<>("GBP_USD");
        TradeableInstrument<String> gbpchf = new TradeableInstrument<>("GBP_CHF");
        TradeableInstrument<String> gbpnzd = new TradeableInstrument<>("GBP_NZD");

        @SuppressWarnings("unchecked")
        Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(gbpusd, gbpchf, gbpnzd);

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);

        BitmexTestUtils.mockHttpInteraction("src/test/resources/currentPrices.txt", mockHttpClient);

        Map<TradeableInstrument<String>, Price<String>> prices = spy.getCurrentPricesForInstruments(instruments);
        assertEquals(instruments.size(), prices.size());
        assertTrue(prices.containsKey(gbpusd));
        assertTrue(prices.containsKey(gbpchf));
        assertTrue(prices.containsKey(gbpnzd));

        Price<String> gbpusdPrice = prices.get(gbpusd);
        assertEquals(new DateTime(TradingUtils.toMillisFromNanos(1442216738184236L)), gbpusdPrice.getPricePoint());
        assertEquals(1.54682, gbpusdPrice.getBidPrice(), BitmexTestConstants.precision);
        assertEquals(1.547, gbpusdPrice.getAskPrice(), BitmexTestConstants.precision);

        Price<String> gbpchfPrice = prices.get(gbpchf);
        assertEquals(new DateTime(TradingUtils.toMillisFromNanos(1442216737600312L)), gbpchfPrice.getPricePoint());
        assertEquals(1.50008, gbpchfPrice.getBidPrice(), BitmexTestConstants.precision);
        assertEquals(1.50058, gbpchfPrice.getAskPrice(), BitmexTestConstants.precision);

        Price<String> gbpnzdPrice = prices.get(gbpnzd);
        assertEquals(new DateTime(TradingUtils.toMillisFromNanos(1442216738184363L)), gbpnzdPrice.getPricePoint());
        assertEquals(2.44355, gbpnzdPrice.getBidPrice(), BitmexTestConstants.precision);
        assertEquals(2.44473, gbpnzdPrice.getAskPrice(), BitmexTestConstants.precision);
    }
}
