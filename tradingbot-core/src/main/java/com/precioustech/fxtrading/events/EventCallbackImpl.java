
package com.precioustech.fxtrading.events;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventCallbackImpl<T> implements EventCallback<T> {

    private final EventBus eventBus;

    @Override
    public void onEvent(EventPayLoad<T> eventPayLoad) {
        eventBus.post(eventPayLoad);
    }

}
