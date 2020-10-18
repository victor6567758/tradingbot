
package com.precioustech.fxtrading.heartbeats;

public interface HeartBeatCallback<T> {

    void onHeartBeat(HeartBeatPayLoad<T> payLoad);
}
