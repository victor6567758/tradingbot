package com.precioustech.fxtrading.events;

public interface EventCallback<T> {

    void onEvent(EventPayLoad<T> eventPayLoad);
}
