package com.precioustech.fxtrading.events.notification.email;

import com.precioustech.fxtrading.events.EventPayLoad;

public interface EmailContentGenerator<T> {

    EmailPayLoad generate(EventPayLoad<T> payLoad);

}
