package com.precioustech.fxtrading.heartbeats;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Uninterruptibles;
import com.precioustech.fxtrading.streaming.heartbeats.HeartBeatStreamingService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHeartBeatService<T> {

    protected static final long MAX_HEARTBEAT_DELAY = 60000L;

    private final Map<String, HeartBeatStreamingService> heartBeatProducerMap = new HashMap<>();
    private final Map<String, HeartBeatPayLoad<T>> payLoadMap = new ConcurrentHashMap<>();

    protected final Collection<HeartBeatStreamingService> heartBeatStreamingServices;
    protected final long initWait = 10000L;

    protected abstract boolean isAlive(HeartBeatPayLoad<T> payLoad);

    long warmUpTime = MAX_HEARTBEAT_DELAY;
    volatile boolean serviceUp = true;

    public AbstractHeartBeatService(
        Collection<HeartBeatStreamingService> heartBeatStreamingServices) {
        this.heartBeatStreamingServices = heartBeatStreamingServices;
        for (HeartBeatStreamingService heartBeatStreamingService : heartBeatStreamingServices) {
            heartBeatProducerMap
                .put(heartBeatStreamingService.getHeartBeatSourceId(), heartBeatStreamingService);
        }
    }

    @PostConstruct
    public void init() {
        heartBeatsObserverThread.start();
    }

    final Thread heartBeatsObserverThread = new Thread(() -> {
        while (serviceUp) {
            Uninterruptibles.sleepUninterruptibly(warmUpTime, TimeUnit.MILLISECONDS);
            for (Map.Entry<String, HeartBeatStreamingService> entry : heartBeatProducerMap.entrySet()) {
                long startWait = initWait;
                while (serviceUp && !isAlive(payLoadMap.get(entry.getKey()))) {
                    entry.getValue().startHeartBeatStreaming();
                    log.warn(String
                        .format(
                            "heartbeat source %s is not responding. just restarted it and will listen for heartbeat after %d ms",
                            entry.getKey(), startWait));
                    Uninterruptibles.sleepUninterruptibly(startWait, TimeUnit.MILLISECONDS);
                    startWait = Math.min(MAX_HEARTBEAT_DELAY, 2 * startWait);
                }
            }
        }
    }, "HeartBeatMonitorThread");

    @Subscribe
    @AllowConcurrentEvents
    public void handleHeartBeats(HeartBeatPayLoad<T> payLoad) {
        this.payLoadMap.put(payLoad.getHeartBeatSource(), payLoad);
    }


}
