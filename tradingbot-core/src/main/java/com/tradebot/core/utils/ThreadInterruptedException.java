package com.tradebot.core.utils;

public class ThreadInterruptedException extends RuntimeException {
    static final long serialVersionUID = -1L;

    public ThreadInterruptedException(Throwable throwable) {
        super(throwable);
    }
}
