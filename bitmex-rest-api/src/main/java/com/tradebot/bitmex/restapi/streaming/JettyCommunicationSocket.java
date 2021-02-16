package com.tradebot.bitmex.restapi.streaming;

import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatPayLoad;
import com.tradebot.core.utils.CommonUtils;
import java.util.concurrent.CountDownLatch;
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
    private static final long CONNECT_WAIT = 2_000;
    private static final long TERMINATION_WAIT = 2_000;

    private static final String PING_COMMAND = "ping";
    private static final String PONG_REPLY = "pong";
    private static final String DID_YOU_FORGET_TO_INITIALIZE_WEBSCOKET = "Did you forget to initialize Webscoket?";


    private final Consumer<String> messageHandler;
    private final Consumer<String> reconnectHandler;
    private final HeartBeatCallback<Long> heartBeatCallback;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong lastPongTime = new AtomicLong(-1L);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private CountDownLatch connectLatch;


    private Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) {

        if (connectLatch == null) {
            throw new IllegalArgumentException("Did you forget to initialize Webscoket?");
        }

        this.session = session;
        startPingingProcess();

        log.info("Websocket connected");
        connectLatch.countDown();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        log.error("Connection closed with code: {}, reason: {}", statusCode, reason);
        if (!stopped.get()) {
            reconnectHandler.accept("Socket disconnection");
        } else {
            log.warn("Shutdown in process...");
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {

        if (log.isDebugEnabled()) {
            log.debug("Message received: {}", message);
        }

        if (message.equals(PONG_REPLY)) {
            validateLastPongTime();
        } else {
            messageHandler.accept(message);
        }

    }

    public void initizalize() {
        connectLatch = new CountDownLatch(1);
    }

    public void waitConnected() {

        if (connectLatch == null) {
            throw new IllegalArgumentException(DID_YOU_FORGET_TO_INITIALIZE_WEBSCOKET);
        }

        try {
            connectLatch.await(CONNECT_WAIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(interruptedException);
        }
    }

    public void subscribe(String message) {
        if (log.isDebugEnabled()) {
            log.debug("Sending command to websocket: " + message);
        }

        Future<Void> future = session.getRemote().sendStringByFuture(message);
        try {
            future.get(COMMAND_DELAY, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new JettyCommunicationSocketException(interruptedException);
        }
        catch (ExecutionException | TimeoutException exception) {
            throw new JettyCommunicationSocketException(exception);
        }
    }

    public void stop() {
        stopped.set(true);
        CommonUtils.commonExecutorServiceShutdown(executorService, TERMINATION_WAIT);
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
                heartBeatCallback.onHeartBeat(new HeartBeatPayLoad<>(responseDelay));
                if (responseDelay >= MAX_PONG_TIME_SECONDS) {
                    log.error("Pong not detected in {}", responseDelay);
                    reconnectHandler.accept("Websocket heartbeat failed");
                }
            } else {
                heartBeatCallback.onHeartBeat(new HeartBeatPayLoad<>(-1L));
            }
        } else {
            log.warn("Shutdown in process...");
        }
    }


}
