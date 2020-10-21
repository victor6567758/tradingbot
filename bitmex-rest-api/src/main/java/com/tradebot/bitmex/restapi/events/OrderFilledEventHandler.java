package com.tradebot.bitmex.restapi.events;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.core.events.EventHandler;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.events.notification.email.EmailContentGenerator;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.trade.TradeInfoService;
import java.util.Set;
import org.json.simple.JSONObject;

public class OrderFilledEventHandler implements EventHandler<JSONObject, OrderEventPayLoad>,
    EmailContentGenerator<JSONObject> {

    private final Set<OrderEvents> orderEventsSupported = Sets.newHashSet(OrderEvents.ORDER_FILLED);
    private final TradeInfoService<Long, String, Long> tradeInfoService;

    public OrderFilledEventHandler(TradeInfoService<Long, String, Long> tradeInfoService) {
        this.tradeInfoService = tradeInfoService;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleEvent(OrderEventPayLoad payLoad) {
        Preconditions.checkNotNull(payLoad);
        if (!orderEventsSupported.contains(payLoad.getEvent())) {
            return;
        }
        JSONObject jsonPayLoad = payLoad.getPayLoad();

        long accountId = (Long) jsonPayLoad.get(BitmexJsonKeys.accountId);
        tradeInfoService.refreshTradesForAccount(accountId);
    }

    @Override
    public EmailPayLoad generate(EventPayLoad<JSONObject> payLoad) {
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        TradeableInstrument<String> instrument = new TradeableInstrument<>(jsonPayLoad
            .containsKey(BitmexJsonKeys.instrument) ? jsonPayLoad.get(BitmexJsonKeys.instrument)
            .toString() : "N/A");
        final String type = jsonPayLoad.get(BitmexJsonKeys.type).toString();
        final long accountId = (Long) jsonPayLoad.get(BitmexJsonKeys.accountId);
        final double accountBalance =
            jsonPayLoad.containsKey(BitmexJsonKeys.accountBalance) ? ((Number) jsonPayLoad
                .get(BitmexJsonKeys.accountBalance)).doubleValue() : 0.0;
        final long orderId = (Long) jsonPayLoad.get(BitmexJsonKeys.id);
        final String emailMsg = String.format(
            "Order event %s received on account %d. Order id=%d. Account balance after the event=%5.2f",
            type,
            accountId, orderId, accountBalance);
        final String subject = String
            .format("Order event %s for %s", type, instrument.getInstrument());
        return new EmailPayLoad(subject, emailMsg);
    }

}
