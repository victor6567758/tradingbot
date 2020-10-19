
package com.tradebot.core.streaming.heartbeats;


public interface HeartBeatStreamingService {

    void startHeartBeatStreaming();

    void stopHeartBeatStreaming();

    String getHeartBeatSourceId();
}
