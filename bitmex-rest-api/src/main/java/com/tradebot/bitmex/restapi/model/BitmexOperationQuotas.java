package com.tradebot.bitmex.restapi.model;

import com.tradebot.core.model.OperationResultContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class BitmexOperationQuotas<N> extends OperationResultContext<N> {

    private int xRatelimitLimit = -1;
    private int xRatelimitRemaining = -1;
    private long xRatelimitReset = -1;
    private int xRatelimitRemaining1s = -1;

    public BitmexOperationQuotas(N data, String message) {
        super(data, message);
    }

    public BitmexOperationQuotas(N data) {
        super(data);
    }

}
