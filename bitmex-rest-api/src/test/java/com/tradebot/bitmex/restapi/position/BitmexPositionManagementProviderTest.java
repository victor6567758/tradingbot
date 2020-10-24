package com.tradebot.bitmex.restapi.position;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.tradebot.bitmex.restapi.BitmexTestConstants;
//import com.tradebot.bitmex.restapi.BitmexTestUtils;
//import com.tradebot.core.TradingSignal;
//import com.tradebot.core.instrument.TradeableInstrument;
//import com.tradebot.core.position.Position;
//import java.util.Collection;
//import java.util.Iterator;
//import org.apache.http.client.methods.HttpDelete;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.junit.Test;
//
//
//public class BitmexPositionManagementProviderTest {
//
//    @Test
//    public void positionForAccountTest() throws Exception {
//        BitmexPositionManagementProvider service = new BitmexPositionManagementProvider(
//            BitmexTestConstants.url,
//            BitmexTestConstants.accessToken);
//        final TradeableInstrument<String> gbpchf = new TradeableInstrument<>("GBP_CHF");
//        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/positions/GBP_CHF", service
//            .getPositionForInstrumentUrl(BitmexTestConstants.accountId, gbpchf));
//        BitmexPositionManagementProvider spy = doMockStuff(
//            "src/test/resources/positionForInstrument.txt", service);
//        Position<String> position = spy
//            .getPositionForInstrument(BitmexTestConstants.accountId, gbpchf);
//        assertNotNull(position);
//        assertEquals(gbpchf, position.getInstrument());
//        assertEquals(TradingSignal.LONG, position.getSide());
//        assertEquals(1.3093, position.getAveragePrice(), BitmexTestConstants.precision);
//        assertEquals(4516L, position.getUnits());
//    }
//
//    @Test
//    public void closePositionTest() throws Exception {
//        BitmexPositionManagementProvider service = new BitmexPositionManagementProvider(
//            BitmexTestConstants.url,
//            BitmexTestConstants.accessToken);
//        BitmexPositionManagementProvider spy = doMockStuff(
//            "src/test/resources/positionForInstrument.txt",
//            service);/*giving a filename although its of not much use here*/
//        boolean success = spy
//            .closePosition(BitmexTestConstants.accountId, new TradeableInstrument<>("AUD_NZD"));
//        assertTrue(success);
//        verify(spy.getHttpClient(), times(1)).execute(any(HttpDelete.class));
//    }
//
//    private BitmexPositionManagementProvider doMockStuff(String fname,
//        BitmexPositionManagementProvider service)
//        throws Exception {
//        BitmexPositionManagementProvider spy = spy(service);
//        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
//        when(spy.getHttpClient()).thenReturn(mockHttpClient);
//        BitmexTestUtils.mockHttpInteraction(fname, mockHttpClient);
//        return spy;
//    }
//
//    @Test
//    public void positionsForAccountTest() throws Exception {
//        BitmexPositionManagementProvider service = new BitmexPositionManagementProvider(
//            BitmexTestConstants.url,
//            BitmexTestConstants.accessToken);
//        assertEquals("https://api-fxtrade.oanda.com/v1/accounts/123456/positions", service
//            .getPositionsForAccountUrl(BitmexTestConstants.accountId));
//
//        BitmexPositionManagementProvider spy = doMockStuff(
//            "src/test/resources/positionsForAccount123456.txt", service);
//        Collection<Position<String>> positions = spy
//            .getPositionsForAccount(BitmexTestConstants.accountId);
//        assertEquals(4, positions.size());
//        Iterator<Position<String>> itr = positions.iterator();
//
//        Position<String> pos1 = itr.next();
//        Position<String> pos2 = itr.next();
//        Position<String> pos3 = itr.next();
//        Position<String> pos4 = itr.next();
//
//        assertEquals("EUR_USD", pos1.getInstrument().getInstrument());
//        assertEquals(6723L, pos1.getUnits());
//        assertEquals(TradingSignal.SHORT, pos1.getSide());
//        assertEquals(1.2419, pos1.getAveragePrice(), BitmexTestConstants.precision);
//
//        assertEquals("GBP_USD", pos2.getInstrument().getInstrument());
//        assertEquals(3000L, pos2.getUnits());
//        assertEquals(TradingSignal.SHORT, pos2.getSide());
//        assertEquals(1.5982, pos2.getAveragePrice(), BitmexTestConstants.precision);
//
//        assertEquals("USD_JPY", pos3.getInstrument().getInstrument());
//        assertEquals(2388L, pos3.getUnits());
//        assertEquals(TradingSignal.LONG, pos3.getSide());
//        assertEquals(112.455, pos3.getAveragePrice(), BitmexTestConstants.precision);
//
//        assertEquals("EUR_CHF", pos4.getInstrument().getInstrument());
//        assertEquals(11020L, pos4.getUnits());
//        assertEquals(TradingSignal.SHORT, pos4.getSide());
//        assertEquals(1.2306, pos4.getAveragePrice(), BitmexTestConstants.precision);
//
//    }
//}
