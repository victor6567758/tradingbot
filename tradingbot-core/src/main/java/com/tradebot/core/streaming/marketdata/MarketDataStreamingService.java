
package com.tradebot.core.streaming.marketdata;


public interface MarketDataStreamingService {

    void init();

    void shutdown();

    void startMarketDataStreaming();

    void stopMarketDataStreaming();

}
