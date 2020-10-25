package com.tradebot.bitmex.restapi.streaming;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@RequiredArgsConstructor
@Slf4j
@WebSocket(maxTextMessageSize = (64 * 1024 * 100), maxBinaryMessageSize = -1)
public class JettyCommunicationSocket {

    private static final long PING_DELAY = 10_000;
    private static final long COMMAND_DELAY = 2_000;
    private static final long MAX_PONG_TIME_SECONDS = 20_000;
    private static final String PING_COMMAND = "ping";

    private final Consumer<String> messageHandler;
    private final Consumer<String> reconnectHandler;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong lastPongTime = new AtomicLong(-1L);
    private final AtomicBoolean stopped = new AtomicBoolean(false);


    private Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        startPingingProcess();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        log.error("Connection close with code: {}, reason: {}", statusCode, reason);
        if (!stopped.get()) {
            reconnectHandler.accept("Socket disconnection");
        } else {
            log.warn("Shutdown in process...");
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        // can come from multiple threads

        if (message.equals("Pong")) {
            validateLastPongTime();
        }

        if (log.isDebugEnabled()) {
            log.debug("Message received: {}", message);
        }

        messageHandler.accept(message);
    }

    public void subscribe(String message) {
        if (log.isDebugEnabled()) {
            log.debug("Sending command to websocket: " + message);
        }

        Future<Void> future = session.getRemote().sendStringByFuture(message);
        try {
            future.get(COMMAND_DELAY, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            throw new JettyCommunicationSocketException(exception);
        }
    }

    public void stop() {
        stopped.set(true);
    }


    private void startPingingProcess() {
        executorService.scheduleWithFixedDelay(() -> {
            session.getRemote().sendStringByFuture(PING_COMMAND);
            if (log.isDebugEnabled()) {
                log.debug("ping command {} sent", PING_COMMAND);
            }
        }, PING_DELAY, PING_DELAY, TimeUnit.MILLISECONDS);
    }

    private void validateLastPongTime() {
        if (!stopped.get()) {
            long lastTime = lastPongTime.getAndSet(System.currentTimeMillis());
            if (lastTime > 0) {
                long responseDelay = System.currentTimeMillis() - lastTime;
                if (responseDelay >= MAX_PONG_TIME_SECONDS) {
                    log.error("Pong not detected in {}", responseDelay);
                    reconnectHandler.accept("Websocket heartbeat failed");
                }
            }
        } else {
            log.warn("Shutdown in process...");
        }
    }

}
