package com.tradebot.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExecutionType {
    NEW("New"),

    TRADE("Trade"),

    TRIGGRED_OR_ACTIVATED_BY_SYSTEM("TriggeredOrActivatedBySystem"),

    REJECTED("Rejected"),

    CANCELLED("Canceled"),

    REPLACED("Replaced"),

    DONE_FOR_DAY("Done for day"),

    PENDING_CANCEL("Pending Cancel"),

    PENDING_NEW("Pending New"),

    EXPIRED("Expired");

    private final String executionTypeText;
}
