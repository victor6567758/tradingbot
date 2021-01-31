package com.tradebot.response.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class DataResponseMessage<T> {
    private final T message;
}
