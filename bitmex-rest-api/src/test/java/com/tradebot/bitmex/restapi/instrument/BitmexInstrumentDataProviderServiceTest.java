package com.tradebot.bitmex.restapi.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.OandaTestUtils;
import com.tradebot.core.instrument.TradeableInstrument;
import java.util.Collection;
import java.util.Iterator;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;


public class BitmexInstrumentDataProviderServiceTest {

    @Test
    public void allInstruments() throws Exception {
        final BitmexInstrumentDataProviderService service = new BitmexInstrumentDataProviderService(
            BitmexTestConstants.url, BitmexTestConstants.accountId,
            BitmexTestConstants.accessToken);
        assertEquals(
            "https://api-fxtrade.oanda.com/v1/instruments?accountId=123456&fields=instrument%2Cpip%2CinterestRate",
            service.getInstrumentsUrl());
        BitmexInstrumentDataProviderService spy = spy(service);
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);
        OandaTestUtils.mockHttpInteraction("src/test/resources/instruments.txt", mockHttpClient);
        Collection<TradeableInstrument<String>> allInstruments = spy.getInstruments();
        assertEquals(2, allInstruments.size());
        Iterator<TradeableInstrument<String>> itr = allInstruments.iterator();

        TradeableInstrument<String> instrument1 = itr.next();
        assertNotNull(instrument1.getInstrumentPairInterestRate());
        assertEquals("AUD_CAD", instrument1.getInstrument());
        assertEquals(0.0001, instrument1.getPip(), BitmexTestConstants.precision);
        assertEquals(0.0164,
            instrument1.getInstrumentPairInterestRate().getBaseCurrencyBidInterestRate(),
            BitmexTestConstants.precision);
        assertEquals(0.0274,
            instrument1.getInstrumentPairInterestRate().getBaseCurrencyAskInterestRate(),
            BitmexTestConstants.precision);
        assertEquals(0.002,
            instrument1.getInstrumentPairInterestRate().getQuoteCurrencyBidInterestRate(),
            BitmexTestConstants.precision);
        assertEquals(0.008,
            instrument1.getInstrumentPairInterestRate().getQuoteCurrencyAskInterestRate(),
            BitmexTestConstants.precision);

        TradeableInstrument<String> instrument2 = itr.next();
        assertNotNull(instrument2.getInstrumentPairInterestRate());
        assertEquals("AUD_CHF", instrument2.getInstrument());
        assertEquals(0.0001, instrument2.getPip(), BitmexTestConstants.precision);
        assertEquals(0.0164,
            instrument2.getInstrumentPairInterestRate().getBaseCurrencyBidInterestRate(),
            BitmexTestConstants.precision);
        assertEquals(0.0274,
            instrument2.getInstrumentPairInterestRate().getBaseCurrencyAskInterestRate(),
            BitmexTestConstants.precision);
        assertEquals(-0.013,
            instrument2.getInstrumentPairInterestRate().getQuoteCurrencyBidInterestRate(),
            BitmexTestConstants.precision);
        assertEquals(0.003,
            instrument2.getInstrumentPairInterestRate().getQuoteCurrencyAskInterestRate(),
            BitmexTestConstants.precision);
    }
}
