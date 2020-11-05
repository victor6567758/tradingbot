package com.tradebot.bitmex.restapi.events.handler;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.events.payload.BitmexExecutionEventPayload;
import com.tradebot.bitmex.restapi.model.websocket.BitmexExecution;
import com.tradebot.core.events.EventHandler;
import com.tradebot.core.events.EventPayLoadToTweet;
import com.tradebot.core.events.notification.email.EmailContentGenerator;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import com.tradebot.core.trade.TradeInfoService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BitmexExecutionEventHandlerImpl implements
    EventHandler<BitmexExecution, BitmexExecutionEventPayload>,
    EmailContentGenerator<BitmexExecution, BitmexExecutionEventPayload>,
    EventPayLoadToTweet<BitmexExecution, BitmexExecutionEventPayload> {

    private final TradeInfoService<Long, String, Long> tradeInfoService;
    private final long accountId;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleEvent(BitmexExecutionEventPayload payLoad) {
        Preconditions.checkNotNull(payLoad);

        tradeInfoService.refreshTradesForAccount(accountId);
    }

    @Override
    public EmailPayLoad generate(BitmexExecutionEventPayload payLoad) {
        return new EmailPayLoad(String.format("Execution event for account %d", accountId), payLoad.getPayLoad().toString());
    }

    @Override
    public String toTweet(BitmexExecutionEventPayload payLoad) {
        return String.format("Execution event for account %d", accountId);
    }
}


