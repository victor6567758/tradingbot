package com.tradebot.event.callback;

import com.tradebot.core.marketdata.historic.CandleStick;

public interface TradeBinPayloadSinkCallBack {
    void onTradeBinCallback(CandleStick candleStick);
}
