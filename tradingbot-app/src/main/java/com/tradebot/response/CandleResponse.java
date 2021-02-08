package com.tradebot.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandleResponse {
    private double open;
    private double high;
    private double low;
    private double close;
    private long dateTime;
}
