package com.tradebot.event.callback;

import com.tradebot.core.marketdata.MarketDataPayLoad;

public interface MarketDataPayLoadSinkCallback {

    void onMarketEvent(MarketDataPayLoad marketDataPayLoad);
}
