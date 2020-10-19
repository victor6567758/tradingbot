package com.tradebot.core.heartbeats;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Uninterruptibles;
import com.tradebot.core.streaming.heartbeats.HeartBeatStreamingService;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHeartBeatService<T> {

    protected static final long MAX_HEARTBEAT_DELAY = 60000L;
    protected static final long INIT_WAIT = 10000L;
    protected static final long MAX_SHUTDOWN_WAIT = 1000L;

    private final Map<String, HeartBeatStreamingService> heartBeatProducerMap;
    private final Map<String, HeartBeatPayLoad<T>> payLoadMap = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final Thread heartBeatsObserverThread;

    protected abstract boolean isAlive(HeartBeatPayLoad<T> payLoad);

    public AbstractHeartBeatService(
        Collection<HeartBeatStreamingService> heartBeatStreamingServices,
        long warmUpTime) {

        heartBeatProducerMap = heartBeatStreamingServices.stream()
            .collect(Collectors
                .toMap(HeartBeatStreamingService::getHeartBeatSourceId, Function.identity()));

        heartBeatsObserverThread = new Thread(() -> {
            running.set(true);
            stopped.set(false);

            while (running.get()) {
                Uninterruptibles
                    .sleepUninterruptibly(warmUpTime >= 0 ? warmUpTime : MAX_HEARTBEAT_DELAY,
                        TimeUnit.MILLISECONDS);

                for (Map.Entry<String, HeartBeatStreamingService> entry : heartBeatProducerMap
                    .entrySet()) {
                    long startWait = INIT_WAIT;
                    while (running.get() && !isAlive(payLoadMap.get(entry.getKey()))) {
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
            stopped.set(true);
        }, "HeartBeatMonitorThread");
    }

    public void stop() {
        boolean wasRunning = running.getAndSet(false);
        if (!wasRunning) {
            log.warn("Service already stopped");
        }

        try {
            heartBeatsObserverThread.join(MAX_SHUTDOWN_WAIT);
        } catch (InterruptedException interruptedException) {
            log.warn("Cannot stop observer thread", interruptedException);
        }

    }

    public boolean isAlive() {
        return stopped.get();
    }

    @PostConstruct
    public void init() {
        heartBeatsObserverThread.start();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handleHeartBeats(HeartBeatPayLoad<T> payLoad) {
        this.payLoadMap.put(payLoad.getSource(), payLoad);
    }


}
