package com.tradebot.core.heartbeats;

import com.tradebot.core.streaming.heartbeats.HeartBeatStreamingService;
import java.util.Collection;
import org.joda.time.DateTime;

public class DefaultHeartBeatService extends AbstractHeartBeatService<DateTime> {

    public DefaultHeartBeatService(Collection<HeartBeatStreamingService> heartBeatStreamingServices,
        long warmUpTime, long startWait) {
        super(heartBeatStreamingServices, warmUpTime, startWait);
    }

    @Override
    protected boolean isTerminated(HeartBeatPayLoad<DateTime> payLoad) {
        return payLoad != null
            && (DateTime.now().getMillis() - payLoad.getPayLoad().getMillis()) < MAX_HEARTBEAT_DELAY;
    }

}
