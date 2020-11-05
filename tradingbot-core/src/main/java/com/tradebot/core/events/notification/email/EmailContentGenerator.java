package com.tradebot.core.events.notification.email;

import com.tradebot.core.events.EventPayLoad;

public interface EmailContentGenerator<K, T extends EventPayLoad<K>> {

    EmailPayLoad generate(T payLoad);

}
