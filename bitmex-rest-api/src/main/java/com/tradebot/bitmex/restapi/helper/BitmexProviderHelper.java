package com.tradebot.bitmex.restapi.helper;

import com.tradebot.core.TradingConstants;
import com.tradebot.core.helper.ProviderHelper;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.utils.TradingUtils;

public class BitmexProviderHelper implements ProviderHelper<String> {

    @Override
    public String fromIsoFormat(String instrument) {
        return BitmexUtils.isoCcyToExchangeCcy(instrument);
    }

    @Override
    public String fromPairSeparatorFormat(String instrument) {
        String[] pair = TradingUtils.splitInstrumentPair(instrument);
        return String.format("%s%s%s", pair[0], BitmexConstants.CCY_PAIR_SEP, pair[1]);
    }

    @Override
    public String toIsoFormat(String instrument) {
        String[] tokens = TradingUtils.splitCcyPair(instrument, TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
        return tokens[0] + tokens[1];
    }

    @Override
    public String getLongNotation() {
        return BitmexConstants.BUY;
    }

    @Override
    public String getShortNotation() {
        return BitmexConstants.SELL;
    }

}
