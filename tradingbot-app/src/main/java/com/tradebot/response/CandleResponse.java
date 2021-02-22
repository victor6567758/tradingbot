package com.tradebot.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class CandleResponse {

    private double open;
    private double high;
    private double low;
    private double close;
    private long dateTime;
}
