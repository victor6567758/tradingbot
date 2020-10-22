package com.tradebot.app;


import com.tradebot.core.BaseTradingConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradingConfig extends BaseTradingConfig {

    private String mailTo;
    private int fadeTheMoveJumpReqdToTrade;
    private int fadeTheMoveDistanceToTrade;
    private int fadeTheMovePipsDesired;
    private int fadeTheMovePriceExpiry;


}
