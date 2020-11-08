package com.tradebot.bitmex.restapi.events.handler;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.events.payload.BitmexTradeEventPayload;
import com.tradebot.bitmex.restapi.model.BitmexTrade;
import com.tradebot.core.events.EventHandler;
import com.tradebot.core.events.EventPayLoadToTweet;
import com.tradebot.core.events.notification.email.EmailContentGenerator;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import com.tradebot.core.trade.TradeInfoService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BitmexTradeEventHandlerImpl implements
    EventHandler<BitmexTrade, BitmexTradeEventPayload>,
    EmailContentGenerator<BitmexTrade, BitmexTradeEventPayload>,
    EventPayLoadToTweet<BitmexTrade, BitmexTradeEventPayload> {

    private final TradeInfoService<Long, String, Long> tradeInfoService;
    private final long accountId;


    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleEvent(BitmexTradeEventPayload payLoad) {
        Preconditions.checkNotNull(payLoad);
        tradeInfoService.refreshTradesForAccount(accountId);
    }

    @Override
    public EmailPayLoad generate(BitmexTradeEventPayload payLoad) {
        return new EmailPayLoad(String.format("Order event for account %d", accountId), payLoad.getPayLoad().toString());
    }

    @Override
    public String toTweet(BitmexTradeEventPayload payLoad) {
        return String.format("Order event for account %d", accountId);

    }
}
