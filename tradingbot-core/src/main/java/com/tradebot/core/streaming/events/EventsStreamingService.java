package com.tradebot.core.streaming.events;


public interface EventsStreamingService {

    void init();

    void shutdown();

    void startEventsStreaming();

    void stopEventsStreaming();
}
