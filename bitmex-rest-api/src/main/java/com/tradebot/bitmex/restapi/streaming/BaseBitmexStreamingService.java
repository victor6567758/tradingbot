package com.tradebot.bitmex.restapi.streaming;

import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.model.BitmexResponse;
import com.tradebot.bitmex.restapi.utils.BitmexJsonBuilder;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.function.Consumer;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@Slf4j
public abstract class BaseBitmexStreamingService {

    private static final String GET_URL_RELATIME = "GET" + "/realtime";

    private static final Gson GSON = BitmexJsonBuilder.buildJson();

    @Getter
    @RequiredArgsConstructor
    public static class MappingFunction {

        @Setter
        private boolean enabled;
        private final Consumer<String> consumer;
        private final String tableName;

        public void invoke(String message) {
            if (enabled) {
                consumer.accept(message);
            }
        }
    }

    @Getter
    protected final JettyCommunicationSocket jettyCommunicationSocket;

    protected final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();


    protected WebSocketClient client;

    private MappingFunction[] mappingFunctions;

    public BaseBitmexStreamingService(HeartBeatCallback<Long> heartBeatCallback) {
        BaseBitmexStreamingService pThis = this;
        jettyCommunicationSocket = new JettyCommunicationSocket(
            pThis::onInternalMessageHandler,
            reason -> {
                log.warn("Reconnecting: {}", reason);
                pThis.init();
                pThis.startSubscribedStreaming();
            }, heartBeatCallback);
    }

    @SneakyThrows
    public void init() {

        // start raises general Exception
        client = new WebSocketClient();
        client.start();

        jettyCommunicationSocket.initizalize();
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

    protected abstract String extractSubscribeTopic(String subscribeElement);

    protected abstract void startSubscribedStreaming();

    protected String buildSubscribeCommand(String... args) {
        return buildWebsocketCommandJson("subscribe", args);
    }

    protected String buildUnSubscribeCommand(String... args) {
        return buildWebsocketCommandJson("unsubscribe", args);
    }

    protected <T> BitmexResponse<T> parseMessage(String message, TypeToken<BitmexResponse<T>> typeToken) {
        return GSON.fromJson(message, typeToken.getType());
    }

    protected void initMapping(MappingFunction[] mappingFunctions) {
        this.mappingFunctions = mappingFunctions;
    }

    protected MappingFunction resolveMappingFunction(String table) {
        if (StringUtils.isEmpty(table)) {
            throw new IllegalArgumentException("Invalid table name");
        }

        for (MappingFunction mappingFunction : mappingFunctions) {
            if (table.equals(mappingFunction.getTableName())) {
                return mappingFunction;
            }
        }
        throw new IllegalArgumentException("Invalid mapping function for: " + table);
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

    private void onInternalMessageHandler(String message) {
        try {
            JsonElement element = JsonParser.parseString(message);
            if (element.isJsonObject()) {
                JsonElement success = element.getAsJsonObject().get("success");
                if (success != null && success.getAsBoolean()) {
                    enableMappedFunction(element);
                } else {
                    JsonElement table = element.getAsJsonObject().get("table");
                    if (table != null) {
                        resolveMappingFunction(table.getAsString()).invoke(message);
                    }
                }
            }
        } catch (JsonSyntaxException jsonSyntaxException) {
            log.error("Error on parsing Bitmex socket message", jsonSyntaxException);
        }
    }

    private void enableMappedFunction(JsonElement element) {
        JsonElement subscribe = element.getAsJsonObject().get("subscribe");
        if (subscribe != null) {
            String subscribeElement = subscribe.getAsString();
            log.info("Subscribe success status of: {}", subscribeElement);
            resolveMappingFunction(extractSubscribeTopic(subscribeElement)).setEnabled(true);
        }
    }


    private static String buildAuthenticateCommand(String apiKey, long nonce, String signature) {
        return buildWebsocketCommandJson("authKey", apiKey, nonce, signature);
    }

    private static String getApiSignature(String secret, long nonce) {
        String message = GET_URL_RELATIME + nonce;

        Key key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return Hashing.hmacSha256(key).hashBytes(message.getBytes(StandardCharsets.UTF_8)).toString();

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
