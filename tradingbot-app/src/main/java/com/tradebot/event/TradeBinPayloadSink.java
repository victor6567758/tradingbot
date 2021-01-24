package com.tradebot.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.event.callback.TradeBinPayloadSinkCallBack;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class TradeBinPayloadSink {

    public TradeBinPayloadSink(@Lazy TradeBinPayloadSinkCallBack tradeBinPayloadSinkCallBack) {
        this.tradeBinPayloadSinkCallBack = tradeBinPayloadSinkCallBack;
    }

    private final TradeBinPayloadSinkCallBack tradeBinPayloadSinkCallBack;

    @Subscribe
    @AllowConcurrentEvents
    public void onTradeBinCallback(CandleStick candleStick) {
        tradeBinPayloadSinkCallBack.onTradeBinCallback(candleStick);
    }
}
