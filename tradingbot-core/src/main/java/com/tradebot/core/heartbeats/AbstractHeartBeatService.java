package com.tradebot.core.heartbeats;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.core.streaming.heartbeats.HeartBeatStreamingService;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHeartBeatService<T> {

    @AllArgsConstructor
    private static class RetryingJobExecutor {

        private final ScheduledExecutorService executorService = Executors
            .newSingleThreadScheduledExecutor();

        private final Function<Long, Long> executionFunction;
        private final long warmUpTime;
        private long startWait;

        public void execute() {
            executorService.schedule(this::runUntilSuccess, warmUpTime, TimeUnit.MILLISECONDS);
        }

        public void shutdown() {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(startWait * 2, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException interruptedException) {
                executorService.shutdownNow();
                log.warn("Could not shutdown executor service in graceful manner",
                    interruptedException);
            }
        }


        public boolean isTerminated() {
            return executorService.isTerminated();
        }

        private void runUntilSuccess() {
            try {
                startWait = executionFunction.apply(startWait);
                executorService
                    .schedule(this::runUntilSuccess, startWait, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                log.warn("Exception on running Callable", e);
            }

        }
    }

    protected static final long MAX_HEARTBEAT_DELAY = 60000L;
    protected static final long MAX_INIT_WAIT = 10000L;

    private final Map<String, HeartBeatStreamingService> heartBeatProducerMap;
    private final Map<String, HeartBeatPayLoad<T>> payLoadMap = new ConcurrentHashMap<>();

    private final RetryingJobExecutor executorService;

    protected abstract boolean isAlive(HeartBeatPayLoad<T> payLoad);

    public AbstractHeartBeatService(
        Collection<HeartBeatStreamingService> heartBeatStreamingServices, long warmUpTime,
        long startWait) {

        heartBeatProducerMap = heartBeatStreamingServices.stream()
            .collect(Collectors
                .toMap(HeartBeatStreamingService::getHeartBeatSourceId, Function.identity()));

        executorService = new RetryingJobExecutor(
            this::heartbeat,
            warmUpTime >= 0 ? warmUpTime : MAX_INIT_WAIT,
            startWait >= 0 ? startWait : MAX_INIT_WAIT);
    }

    public void stop() {
        executorService.shutdown();
    }

    public boolean isAlive() {
        return executorService.isTerminated();
    }

    @PostConstruct
    public void init() {
        executorService.execute();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handleHeartBeats(HeartBeatPayLoad<T> payLoad) {
        this.payLoadMap.put(payLoad.getSource(), payLoad);
    }

    private long heartbeat(long startWait) {
        long newStartWait = startWait;
        for (Map.Entry<String, HeartBeatStreamingService> entry : heartBeatProducerMap.entrySet()) {
            if (!isAlive(payLoadMap.get(entry.getKey()))) {
                entry.getValue().startHeartBeatStreaming();
                log.warn(
                    "Heartbeat source {} is not responding. just restarted it and will listen for heartbeat after {} ms",
                    entry.getKey(), startWait);
                newStartWait = Math.max(newStartWait, Math.min(MAX_HEARTBEAT_DELAY, 2 * startWait));
            }
        }
        return newStartWait;
    }


}
