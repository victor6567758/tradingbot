
package com.precioustech.fxtrading.streaming.heartbeats;


public interface HeartBeatStreamingService {

    void startHeartBeatStreaming();

    void stopHeartBeatStreaming();

    String getHeartBeatSourceId();
}
