package com.tradebot.core.events;

public interface EventCallback<T> {

    void onEvent(EventPayLoad<T> eventPayLoad);
}
