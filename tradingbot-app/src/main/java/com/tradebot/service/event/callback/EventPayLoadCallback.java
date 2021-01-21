package com.tradebot.service.event.callback;

import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;

public interface EventPayLoadCallback {

    void handleEvent(JsonEventPayLoad eventPayLoad);
}
