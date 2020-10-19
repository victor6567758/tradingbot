package com.tradebot.core.events.notification.email;

import com.tradebot.core.events.EventPayLoad;

public interface EmailContentGenerator<T> {

    EmailPayLoad generate(EventPayLoad<T> payLoad);

}
