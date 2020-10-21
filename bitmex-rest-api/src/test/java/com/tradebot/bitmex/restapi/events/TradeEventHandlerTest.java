package com.tradebot.bitmex.restapi.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.transaction.Transaction;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.trade.TradeInfoService;
import org.json.simple.JSONObject;
import org.junit.Test;

public class TradeEventHandlerTest {

    @Test
    public void generatePayLoad() {
        TradeEventHandler eventHandler = new TradeEventHandler(null, null, null);
        // its ok if the we pass null to the constructor here as its not used
        JSONObject jsonPayLoad = mock(JSONObject.class);
        EventPayLoad<JSONObject> payLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED,
            jsonPayLoad);
        when(jsonPayLoad.get(BitmexJsonKeys.instrument)).thenReturn("GBP_USD");
        when(jsonPayLoad.get(BitmexJsonKeys.type)).thenReturn(TradeEvents.TAKE_PROFIT_FILLED.name());
        when(jsonPayLoad.get(BitmexJsonKeys.accountId)).thenReturn(BitmexTestConstants.accountId);
        when(jsonPayLoad.get(BitmexJsonKeys.accountBalance)).thenReturn(100.00);
        when(jsonPayLoad.get(BitmexJsonKeys.tradeId)).thenReturn(BitmexTestConstants.tradeId);
        when(jsonPayLoad.get(BitmexJsonKeys.pl)).thenReturn(22.45);
        when(jsonPayLoad.get(BitmexJsonKeys.interest)).thenReturn(-1.45);
        when(jsonPayLoad.get(BitmexJsonKeys.units)).thenReturn(10000L);
        EmailPayLoad emailPayLoad = eventHandler.generate(payLoad);
        assertEquals("Trade event TAKE_PROFIT_FILLED for GBP_USD", emailPayLoad.getSubject());
        assertEquals(
            "Trade event TAKE_PROFIT_FILLED received for account 123456. Trade id=1800805337. Pnl=22.450, Interest=-1.450, Trade Units=10000. Account balance after the event=100.00",
            emailPayLoad.getBody());
    }

    @Test
    public void unSupportedTradeEvent() {
        JSONObject jsonPayLoad = mock(JSONObject.class);
        TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.MIGRATE_TRADE_CLOSE,
            jsonPayLoad);
        @SuppressWarnings("unchecked")
        TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
        TradeEventHandler eventHandler = new TradeEventHandler(tradeInfoService, null, null);
        eventHandler.handleEvent(payLoad);
        verify(tradeInfoService, times(0)).refreshTradesForAccount(BitmexTestConstants.accountId);
    }

    @Test
    public void tradeEvent() {
        JSONObject jsonPayLoad = mock(JSONObject.class);
        TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED,
            jsonPayLoad);
        when(jsonPayLoad.get(BitmexJsonKeys.accountId)).thenReturn(BitmexTestConstants.accountId);
        @SuppressWarnings("unchecked")
        TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
        TradeEventHandler eventHandler = new TradeEventHandler(tradeInfoService, null, null);
        eventHandler.handleEvent(payLoad);
        verify(tradeInfoService, times(1)).refreshTradesForAccount(BitmexTestConstants.accountId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void payLoadToTweet() {
        JSONObject jsonPayLoad = mock(JSONObject.class);
        TradeableInstrument<String> eurchf = new TradeableInstrument<>("EUR_CHF");
        TradeEventPayLoad payLoad = new TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED,
            jsonPayLoad);
        when(jsonPayLoad.get(BitmexJsonKeys.instrument)).thenReturn(eurchf.getInstrument());
        when(jsonPayLoad.get(BitmexJsonKeys.units)).thenReturn(200l);
        when(jsonPayLoad.get(BitmexJsonKeys.price)).thenReturn(1.10325);
        when(jsonPayLoad.get(BitmexJsonKeys.tradeId)).thenReturn(BitmexTestConstants.tradeId);
        when(jsonPayLoad.get(BitmexJsonKeys.accountId)).thenReturn(BitmexTestConstants.accountId);
        TransactionDataProvider<Long, Long, String> transactionDataProvider = mock(
            TransactionDataProvider.class);

        // test profit scenario with short
        Transaction<Long, Long, String> transaction = mock(Transaction.class);
        when(transactionDataProvider.getTransaction(
            BitmexTestConstants.tradeId, BitmexTestConstants.accountId))
            .thenReturn(transaction);
        when(transaction.getSide()).thenReturn(TradingSignal.SHORT);
        when(transaction.getPrice()).thenReturn(1.11);
        InstrumentService<String> instrumentService = mock(InstrumentService.class);
        when(instrumentService.getPipForInstrument(eq(eurchf))).thenReturn(0.0001);
        TradeEventHandler eventHandler = new TradeEventHandler(null, transactionDataProvider,
            instrumentService);
        String tweet = eventHandler.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("Closed SHORT 200 units of #EURCHF@1.10325 for 67.5 pips.", tweet);

        // test loss scenario with short
        transaction = mock(Transaction.class);
        when(transactionDataProvider.getTransaction(
            BitmexTestConstants.tradeId, BitmexTestConstants.accountId))
            .thenReturn(transaction);
        when(transaction.getSide()).thenReturn(TradingSignal.SHORT);
        when(transaction.getPrice()).thenReturn(1.10);
        tweet = eventHandler.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("Closed SHORT 200 units of #EURCHF@1.10325 for -32.5 pips.", tweet);

        // test profit scenario with long
        transaction = mock(Transaction.class);
        when(transactionDataProvider.getTransaction(
            BitmexTestConstants.tradeId, BitmexTestConstants.accountId))
            .thenReturn(transaction);
        when(transaction.getSide()).thenReturn(TradingSignal.LONG);
        when(transaction.getPrice()).thenReturn(1.10);
        tweet = eventHandler.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("Closed LONG 200 units of #EURCHF@1.10325 for 32.5 pips.", tweet);

        // test loss scenario with long
        transaction = mock(Transaction.class);
        when(transactionDataProvider.getTransaction(
            BitmexTestConstants.tradeId, BitmexTestConstants.accountId))
            .thenReturn(transaction);
        when(transaction.getSide()).thenReturn(TradingSignal.LONG);
        when(transaction.getPrice()).thenReturn(1.11);
        tweet = eventHandler.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("Closed LONG 200 units of #EURCHF@1.10325 for -67.5 pips.", tweet);

        when(transactionDataProvider.getTransaction(
            BitmexTestConstants.tradeId, BitmexTestConstants.accountId))
            .thenReturn(null);
        when(jsonPayLoad.get(BitmexJsonKeys.side)).thenReturn("sell");
        tweet = eventHandler.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("Closed LONG 200 units of #EURCHF@1.10325.", tweet);
        // unsupported event
        TradeEventPayLoad payload2 = new TradeEventPayLoad(TradeEvents.MIGRATE_TRADE_CLOSE,
            jsonPayLoad);
        assertNull(eventHandler.toTweet(payload2));

    }
}
