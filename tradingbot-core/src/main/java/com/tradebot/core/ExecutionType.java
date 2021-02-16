package com.tradebot.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExecutionType {
    NEW("New"),

    TRADE("Trade"),

    REJECTED("Rejected"),

    CANCELLED("Canceled"),

    REPLACED("Replaced"),

    DONE_FOR_DAY("Done for day"),

    PENDING_CANCEL("Pending Cancel"),

    PENDING_NEW("Pending New"),

    EXPIRED("Expired");

    private final String executionTypeText;
}
