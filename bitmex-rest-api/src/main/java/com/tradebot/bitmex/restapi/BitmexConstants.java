package com.tradebot.bitmex.restapi;

import com.tradebot.core.TradingConstants;
import lombok.experimental.UtilityClass;


@UtilityClass
public final class BitmexConstants {

    public static final String CCY_PAIR_SEP = TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE;
    public static final String BITMEX_FAILURE = "Bitmex API failure %s";
}
