package com.tradebot.bitmex.restapi.streaming;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@Slf4j
public abstract class BaseBitmexStreamingService2 {

    protected final JettyCommunicationSocket jettyCommunicationSocket;
    protected final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();
    protected WebSocketClient client;

    public BaseBitmexStreamingService2() {
        BaseBitmexStreamingService2 pThis = this;
        jettyCommunicationSocket = new JettyCommunicationSocket(
            pThis::onMessageHandlerInternal,
            reason -> {
                log.warn("Reconnecting: {}", reason);
                pThis.init();
            }
        );
    }

    @SneakyThrows
    public void init() {
        // start raises general Exception
        client = new WebSocketClient();
        client.start();

        client.connect(jettyCommunicationSocket,
            new URI(bitmexAccountConfiguration.getBitmex().getApi().getWebSocketUrl()), new ClientUpgradeRequest());
        jettyCommunicationSocket.waitConnected();

        authenticate();
        log.info("Websocket client fully connected");
    }

    public void shutdown() {
        try {
            jettyCommunicationSocket.stop();
        } finally {
            try {
                client.stop();
            } catch (Exception exception) {
                // client.stop() Exception signature
                log.error("Error on websocket disconnect", exception);
            }
        }

    }

    protected abstract void onMessageHandler(String message);

    protected abstract void connect();

    protected abstract void disconnect();

    protected String buildSubscribeCommand(String... args) {
        return buildWebsocketCommandJson("subscribe", args);
    }

    protected String buildUnSubscribeCommand(String... args) {
        return buildWebsocketCommandJson("unsubscribe", args);
    }

    private boolean processWelcomeReply(String message) {
        return true;
    }

    private void onMessageHandlerInternal(String message) {
        processWelcomeReply(message);
        onMessageHandler(message);
    }

    private void authenticate() {
        long nonce = System.currentTimeMillis();
        String signature = getApiSignature(
            bitmexAccountConfiguration.getBitmex().getApi().getSecret(), nonce);

        jettyCommunicationSocket.subscribe(buildAuthenticateCommand(
            bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            nonce,
            signature));
    }

    private static String buildAuthenticateCommand(String apiKey, long nonce, String signature) {
        return buildWebsocketCommandJson("authKey", apiKey, nonce, signature);
    }

    private static String getApiSignature(String secret, long nonce) {
        String message = "GET" + "/realtime" + nonce;

        Key key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        HashCode hashCode = Hashing.hmacSha256(key).hashBytes(message.getBytes(StandardCharsets.UTF_8));
        return hashCode.toString();

    }

    private static String buildWebsocketCommandJson(String command, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"op\": \"")
            .append(command)
            .append("\", \"args\": [");
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                sb.append("\"");
            }
            sb.append(args[i]);
            if (args[i] instanceof String) {
                sb.append("\"");
            }
            if (i == args.length - 1) {
                sb.append("]");
            } else {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }


}
