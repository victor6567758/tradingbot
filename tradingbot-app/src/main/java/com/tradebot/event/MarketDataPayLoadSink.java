package com.tradebot.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.core.marketdata.MarketDataPayLoad;
import com.tradebot.event.callback.MarketDataPayLoadSinkCallback;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
public class MarketDataPayLoadSink {

    public MarketDataPayLoadSink(@Lazy MarketDataPayLoadSinkCallback marketDataPayLoadSinkCallback) {
        this.marketDataPayLoadSinkCallback = marketDataPayLoadSinkCallback;
    }

    private final MarketDataPayLoadSinkCallback marketDataPayLoadSinkCallback;

    @Subscribe
    @AllowConcurrentEvents
    public void onMarketEvent(MarketDataPayLoad marketDataPayLoad) {
        marketDataPayLoadSinkCallback.onMarketEvent(marketDataPayLoad);
    }

}
