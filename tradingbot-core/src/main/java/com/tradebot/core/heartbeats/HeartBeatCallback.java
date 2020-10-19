
package com.tradebot.core.heartbeats;

public interface HeartBeatCallback<T> {

    void onHeartBeat(HeartBeatPayLoad<T> payLoad);
}
