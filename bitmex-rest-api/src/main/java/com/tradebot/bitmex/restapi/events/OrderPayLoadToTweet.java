package com.tradebot.bitmex.restapi.events;

import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.events.EventPayLoadToTweet;
import java.util.Set;

import org.json.simple.JSONObject;

import com.google.common.collect.Sets;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;

public class OrderPayLoadToTweet implements EventPayLoadToTweet<JSONObject, OrderEventPayLoad> {

    private final Set<OrderEvents> orderEventsSupported = Sets.newHashSet(OrderEvents.ORDER_FILLED,
            OrderEvents.LIMIT_ORDER_CREATE);

    @Override
    public String toTweet(OrderEventPayLoad payLoad) {
        if (!orderEventsSupported.contains(payLoad.getEvent())) {
            return null;
        }
        final JSONObject jsonPayLoad = payLoad.getPayLoad();
        final String instrument = jsonPayLoad.get(BitmexJsonKeys.instrument).toString();

        final String instrumentAsHashtag = BitmexUtils.bitmexToHashTagCcy(instrument);
        final long tradeUnits = (Long) jsonPayLoad.get(BitmexJsonKeys.units);
        final double price = ((Number) jsonPayLoad.get(BitmexJsonKeys.price)).doubleValue();
        final String side = jsonPayLoad.get(BitmexJsonKeys.side).toString();
        TradingSignal signal = BitmexUtils.toTradingSignal(side);

        switch (payLoad.getEvent()) {
        case ORDER_FILLED:
            return String.format("Opened %s position of %d units for %s@%2.5f", signal.name(), tradeUnits,
                    instrumentAsHashtag, price);
        case LIMIT_ORDER_CREATE:
            return String.format("%s LIMIT order of %d units for %s@%2.5f", signal.name(), tradeUnits,
                    instrumentAsHashtag, price);
        default:
            return null;
        }
    }

}
