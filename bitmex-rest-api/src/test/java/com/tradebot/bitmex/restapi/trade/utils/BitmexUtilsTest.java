package com.tradebot.bitmex.restapi.trade.utils;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import com.tradebot.bitmex.restapi.BitmexConstants;
//import com.tradebot.bitmex.restapi.events.AccountEventPayLoad;
//import com.tradebot.bitmex.restapi.events.AccountEvents;
//import com.tradebot.bitmex.restapi.events.OrderEventPayLoad;
//import com.tradebot.bitmex.restapi.events.OrderEvents;
//import com.tradebot.bitmex.restapi.events.payload.TradeEventPayLoad;
//import com.tradebot.bitmex.restapi.events.TradeEvents;
//import com.tradebot.bitmex.restapi.utils.BitmexUtils;
//import com.tradebot.core.TradingSignal;
//import com.tradebot.core.order.OrderType;
//import org.junit.Test;
//
//
//public class BitmexUtilsTest {
//
//    @Test
//    public void toOandaTransactionTypeTest() {
//        assertEquals(TradeEvents.TRADE_CLOSE, BitmexUtils.toBitmexTransactionType("TRADE_CLOSE"));
//        assertEquals(OrderEvents.ORDER_FILLED, BitmexUtils.toBitmexTransactionType("ORDER_FILLED"));
//        assertEquals(AccountEvents.MARGIN_CLOSEOUT,
//            BitmexUtils.toBitmexTransactionType("MARGIN_CLOSEOUT"));
//        assertNull(BitmexUtils.toBitmexTransactionType("FOO"));
//    }
//
//    @Test
//    public void toOandaEventPayLoadTest() {
//        assertTrue(
//            BitmexUtils.toBitmexEventPayLoad("TRADE_UPDATE", null) instanceof TradeEventPayLoad);
//        assertTrue(
//            BitmexUtils.toBitmexEventPayLoad("ORDER_FILLED", null) instanceof OrderEventPayLoad);
//        assertTrue(BitmexUtils.toBitmexEventPayLoad("FEE", null) instanceof AccountEventPayLoad);
//        assertNull(BitmexUtils.toBitmexEventPayLoad("BAR", null));
//    }
//
//    @Test
//    public void splitOandaCcyTest() {
//        String[] pair = BitmexUtils.splitCcyPair("GBP_USD");
//        assertEquals(2, pair.length);
//        assertEquals("GBP", pair[0]);
//        assertEquals("USD", pair[1]);
//    }
//
//    @Test
//    public void toOandaCcyTest() {
//        assertEquals("USD_ZAR", BitmexUtils.toИшеьучCcy("USD", "ZAR"));
//        try {
//            BitmexUtils.toИшеьучCcy("us", "ZAR");
//            fail("expected exception");
//        } catch (IllegalArgumentException e) {
//            // pass
//        }
//        try {
//            BitmexUtils.toИшеьучCcy("USD", "za");
//            fail("expected exception");
//        } catch (IllegalArgumentException e) {
//            // pass
//        }
//    }
//
//    @Test
//    public void isoCcyToOandaCcyTest() {
//        assertEquals("GBP_CHF", BitmexUtils.isoCcyToOandaCcy("GBPCHF"));
//        try {
//            BitmexUtils.isoCcyToOandaCcy("gbpch");
//            fail("expected exception");
//        } catch (IllegalArgumentException e) {
//            // pass
//        }
//    }
//
//    @Test
//    public void hashTagCcyToOandaCcyTest() {
//        assertEquals("AUD_CAD", BitmexUtils.hashTagCcyToOandaCcy("#AUDCAD"));
//        try {
//            BitmexUtils.hashTagCcyToOandaCcy("$AUDCAD");
//            fail("expected exception");
//        } catch (IllegalArgumentException e) {
//            // pass
//        }
//        try {
//            BitmexUtils.hashTagCcyToOandaCcy("AUDCAD");
//            fail("expected exception");
//        } catch (IllegalArgumentException e) {
//            // pass
//        }
//    }
//
//    @Test
//    public void toTradingSignalTest() {
//        assertEquals(TradingSignal.LONG, BitmexUtils.toTradingSignal(BitmexConstants.BUY));
//        assertEquals(TradingSignal.SHORT, BitmexUtils.toTradingSignal(BitmexConstants.SELL));
//        assertEquals(TradingSignal.NONE, BitmexUtils.toTradingSignal("Sell"));
//        assertEquals(TradingSignal.NONE, BitmexUtils.toTradingSignal("foo"));
//    }
//
//    @Test
//    public void toOrderTypeTest() {
//        assertEquals(OrderType.MARKET, BitmexUtils.toOrderType(BitmexConstants.ORDER_MARKET));
//        assertEquals(OrderType.LIMIT, BitmexUtils.toOrderType(BitmexConstants.ORDER_LIMIT));
//        assertEquals(OrderType.LIMIT,
//            BitmexUtils.toOrderType(BitmexConstants.ORDER_MARKET_IF_TOUCHED));
//        try {
//            BitmexUtils.toOrderType("foo");
//            fail("expected exception");
//        } catch (IllegalArgumentException e) {
//            // pass
//        }
//    }
//}
