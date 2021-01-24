package com.tradebot.event;


import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.events.payload.PayloadAcceptor;
import com.tradebot.bitmex.restapi.events.payload.ProcessedEventVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPayLoadSink {

    private final ProcessedEventVisitor processedEventVisitor;

    @Subscribe
    @AllowConcurrentEvents
    public void onEvent(PayloadAcceptor event) {
        event.accept(processedEventVisitor);
    }


}
