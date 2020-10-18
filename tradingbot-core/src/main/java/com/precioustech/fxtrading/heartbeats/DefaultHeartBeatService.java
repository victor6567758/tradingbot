package com.precioustech.fxtrading.heartbeats;

import java.util.Collection;

import org.joda.time.DateTime;

import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;

public class DefaultHeartBeatService extends AbstractHeartBeatService<DateTime> {

    public DefaultHeartBeatService(Collection<HeartBeatStreamingService> heartBeatStreamingServices) {
        super(heartBeatStreamingServices);
    }

    @Override
    protected boolean isAlive(HeartBeatPayLoad<DateTime> payLoad) {
        return payLoad != null
                && (DateTime.now().getMillis() - payLoad.getHeartBeatPayLoad().getMillis()) < MAX_HEARTBEAT_DELAY;
    }

}
