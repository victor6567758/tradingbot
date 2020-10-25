package com.tradebot.bitmex.restapi.streaming;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@Slf4j
public abstract class BaseBitmexStreamingService2 {

    protected final JettyCommunicationSocket jettyCommunicationSocket;
    protected final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();
    protected final WebSocketClient client = new WebSocketClient();

    public BaseBitmexStreamingService2() {
        BaseBitmexStreamingService2 pThis = this;
        jettyCommunicationSocket = new JettyCommunicationSocket(
            pThis::onMessageHandler,
            reason -> {
                log.warn("Reconnecting: {}", reason);
                pThis.connect();
            }
        );
    }

    public void init() throws Exception {
        // start raises general Exception
        client.start();

        client.connect(jettyCommunicationSocket,
            new URI(bitmexAccountConfiguration.getBitmex().getApi().getWebSocketUrl()), new ClientUpgradeRequest());

        long nonce = System.currentTimeMillis();
        String signature = getApiSignature(
            bitmexAccountConfiguration.getBitmex().getApi().getSecret(), nonce);

        jettyCommunicationSocket.subscribe(buildAuthenticateCommand(
            bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            nonce,
            signature));

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
        return BitmexUtils.buildWebsocketCommandJson("subscribe", args);
    }

    protected String buildUnSubscribeCommand(String... args) {
        return BitmexUtils.buildWebsocketCommandJson("unsubscribe", args);
    }

    protected String buildAuthenticateCommand(String apiKey, long nonce, String signature) {
        return BitmexUtils.buildWebsocketCommandJson("authKey", apiKey, nonce, signature);
    }

    private static String getApiSignature(String secret, long nonce) {
        String message = "GET" + "/realtime" + nonce;

        Key key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        HashCode hashCode = Hashing.hmacSha256(key).hashBytes(message.getBytes(StandardCharsets.UTF_8));
        return hashCode.toString();

    }

}
