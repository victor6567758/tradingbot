package com.tradebot.core.heartbeats;

import java.util.Collection;

import org.joda.time.DateTime;

import com.tradebot.core.streaming.heartbeats.HeartBeatStreamingService;

public class DefaultHeartBeatService extends AbstractHeartBeatService<DateTime> {

    public DefaultHeartBeatService(Collection<HeartBeatStreamingService> heartBeatStreamingServices, long warmUpTime) {
        super(heartBeatStreamingServices, warmUpTime);
    }

    @Override
    protected boolean isAlive(HeartBeatPayLoad<DateTime> payLoad) {
        return payLoad != null
                && (DateTime.now().getMillis() - payLoad.getPayLoad().getMillis()) < MAX_HEARTBEAT_DELAY;
    }

}
