package com.tradebot.bitmex.restapi.streaming;

public class JettyCommunicationSocketException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public JettyCommunicationSocketException(Throwable throwable) {
        super(throwable);
    }
}
