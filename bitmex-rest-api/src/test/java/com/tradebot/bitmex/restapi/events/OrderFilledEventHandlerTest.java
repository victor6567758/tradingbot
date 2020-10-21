package com.tradebot.bitmex.restapi.events;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import com.tradebot.core.trade.TradeInfoService;
import org.json.simple.JSONObject;
import org.junit.Test;

public class OrderFilledEventHandlerTest {

    @Test
    public void generatePayLoad() {
        OrderFilledEventHandler eventHandler = new OrderFilledEventHandler(null);
        JSONObject jsonPayLoad = mock(JSONObject.class);
        EventPayLoad<JSONObject> payLoad = new OrderEventPayLoad(OrderEvents.ORDER_FILLED,
            jsonPayLoad);
        when(jsonPayLoad.containsKey(BitmexJsonKeys.instrument)).thenReturn(true);
        when(jsonPayLoad.get(BitmexJsonKeys.instrument)).thenReturn("GBP_CHF");
        when(jsonPayLoad.get(BitmexJsonKeys.type)).thenReturn(OrderEvents.ORDER_FILLED);
        when(jsonPayLoad.get(BitmexJsonKeys.accountId)).thenReturn(BitmexTestConstants.accountId);
        when(jsonPayLoad.containsKey(BitmexJsonKeys.accountBalance)).thenReturn(true);
        when(jsonPayLoad.get(BitmexJsonKeys.accountBalance)).thenReturn(178.95);
        when(jsonPayLoad.get(BitmexJsonKeys.id)).thenReturn(1002L);
        EmailPayLoad emailPayLoad = eventHandler.generate(payLoad);
        assertEquals("Order event ORDER_FILLED for GBP_CHF", emailPayLoad.getSubject());
        assertEquals(
            "Order event ORDER_FILLED received on account 123456. Order id=1002. Account balance after the event=178.95",
            emailPayLoad.getBody());
    }

    @Test
    public void unSupportedOrderEvent() {
        JSONObject jsonPayLoad = mock(JSONObject.class);
        OrderEventPayLoad payLoad = new OrderEventPayLoad(OrderEvents.ORDER_CANCEL, jsonPayLoad);
        @SuppressWarnings("unchecked")
        TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
        OrderFilledEventHandler eventHandler = new OrderFilledEventHandler(tradeInfoService);
        eventHandler.handleEvent(payLoad);
        verify(tradeInfoService, times(0)).refreshTradesForAccount(BitmexTestConstants.accountId);
    }

    @Test
    public void orderEvent() {
        JSONObject jsonPayLoad = mock(JSONObject.class);
        OrderEventPayLoad payLoad = new OrderEventPayLoad(OrderEvents.ORDER_FILLED, jsonPayLoad);
        when(jsonPayLoad.get(BitmexJsonKeys.accountId)).thenReturn(BitmexTestConstants.accountId);
        @SuppressWarnings("unchecked")
        TradeInfoService<Long, String, Long> tradeInfoService = mock(TradeInfoService.class);
        OrderFilledEventHandler eventHandler = new OrderFilledEventHandler(tradeInfoService);
        eventHandler.handleEvent(payLoad);
        verify(tradeInfoService, times(1)).refreshTradesForAccount(BitmexTestConstants.accountId);
    }
}
