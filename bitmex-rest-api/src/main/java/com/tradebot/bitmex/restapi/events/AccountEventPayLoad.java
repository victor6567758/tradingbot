package com.tradebot.bitmex.restapi.events;

import com.tradebot.core.events.EventPayLoad;
import org.json.simple.JSONObject;


public class AccountEventPayLoad extends EventPayLoad<JSONObject> {

    public AccountEventPayLoad(AccountEvents event, JSONObject payLoad) {
        super(event, payLoad);
    }

}
