package com.tradebot.bitmex.restapi.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.core.instrument.TradeableInstrument;
import org.json.simple.JSONObject;
import org.junit.Test;

public class OrderPayLoadToTweetTest {

    @Test
    public void payLoadToTweet() {
        OrderPayLoadToTweet payLoadToTweet = new OrderPayLoadToTweet();
        JSONObject jsonPayLoad = mock(JSONObject.class);

        TradeableInstrument<String> eurchf = new TradeableInstrument<>("EUR_CHF");
        // TradeEventPayLoad payLoad = new
        // TradeEventPayLoad(TradeEvents.TAKE_PROFIT_FILLED, jsonPayLoad);
        when(jsonPayLoad.get(BitmexJsonKeys.instrument)).thenReturn(eurchf.getInstrument());
        when(jsonPayLoad.get(BitmexJsonKeys.units)).thenReturn(200l);
        when(jsonPayLoad.get(BitmexJsonKeys.price)).thenReturn(1.10325);
        when(jsonPayLoad.get(BitmexJsonKeys.side)).thenReturn("sell");
        OrderEventPayLoad payLoad = new OrderEventPayLoad(OrderEvents.ORDER_FILLED, jsonPayLoad);
        String tweet = payLoadToTweet.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("Opened SHORT position of 200 units for #EURCHF@1.10325", tweet);

        payLoad = new OrderEventPayLoad(OrderEvents.LIMIT_ORDER_CREATE, jsonPayLoad);
        tweet = payLoadToTweet.toTweet(payLoad);
        assertNotNull(tweet);
        assertEquals("SHORT LIMIT order of 200 units for #EURCHF@1.10325", tweet);

        payLoad = new OrderEventPayLoad(OrderEvents.ORDER_CANCEL, jsonPayLoad);
        tweet = payLoadToTweet.toTweet(payLoad);
        assertNull(tweet);
    }
}
