package com.tradebot.core.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = {"data"})
public class OperationResultContext<N> {

    private final N data;
    private final boolean result;
    private final String message;

    public OperationResultContext(N data, String message) {
        this.data = data;
        this.result = false;
        this.message = message;
    }

    public OperationResultContext(N data) {
        this.data = data;
        this.result = true;
        this.message = "OK";
    }
}
