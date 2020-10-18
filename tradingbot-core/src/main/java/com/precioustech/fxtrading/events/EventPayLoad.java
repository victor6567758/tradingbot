
package com.precioustech.fxtrading.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EventPayLoad<T> {
    private final Event event;
    private final T payLoad;
}
