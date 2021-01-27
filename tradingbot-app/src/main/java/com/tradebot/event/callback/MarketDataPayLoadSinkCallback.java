package com.tradebot.event.callback;

import com.tradebot.core.marketdata.Price;

public interface MarketDataPayLoadSinkCallback {

    void onMarketEvent(Price price);
}
