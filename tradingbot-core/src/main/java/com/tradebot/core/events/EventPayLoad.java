
package com.tradebot.core.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class EventPayLoad<T>  {
    private final Event event;
    private final T payLoad;
}
