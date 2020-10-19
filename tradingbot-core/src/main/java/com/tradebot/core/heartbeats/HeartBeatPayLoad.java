package com.tradebot.core.heartbeats;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class HeartBeatPayLoad<T> {

    private final T payLoad;
    private final String source;

    public HeartBeatPayLoad(T payLoad) {
        this(payLoad, StringUtils.EMPTY);
    }

    public HeartBeatPayLoad(T payLoad, String source) {
        this.payLoad = payLoad;
        this.source = source;
    }

}
