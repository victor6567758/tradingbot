package com.tradebot.service.event.callback;

import com.tradebot.bitmex.restapi.events.payload.JsonEventPayLoad;
import com.tradebot.core.events.EventPayLoad;
import org.json.simple.JSONObject;

public interface EventPayLoadCallback {
    void handleEvent(JsonEventPayLoad eventPayLoad);
}
