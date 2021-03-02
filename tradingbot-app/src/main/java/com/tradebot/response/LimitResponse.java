package com.tradebot.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitResponse {
    private int requestLimitPerMinute;
    private int requestRemainingWithin1Sec;
    private int requestRemainingWithinMinute;
    private long limitResetTime;
}
