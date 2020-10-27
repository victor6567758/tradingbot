package com.tradebot.bitmex.restapi.streaming;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.model.websocket.BitmexResponse;
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
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@Slf4j
public abstract class BaseBitmexStreamingService2 {

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

    private final HeartBeatCallback<DateTime> heartBeatCallback;

    protected final JettyCommunicationSocket jettyCommunicationSocket;

    protected final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc, context) ->
            new JsonPrimitive(ISODateTimeFormat.dateTime().print(json)))
        .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) ->
            ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()))
        .create();
    protected WebSocketClient client;

    private MappingFunction[] mappingFunctions;

    public BaseBitmexStreamingService2(HeartBeatCallback<DateTime> heartBeatCallback) {

        this.heartBeatCallback = heartBeatCallback;

        BaseBitmexStreamingService2 pThis = this;
        jettyCommunicationSocket = new JettyCommunicationSocket(
            pThis::onInternalMessageHandler,
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


    protected abstract void connect();

    protected abstract void disconnect();

    protected String buildSubscribeCommand(String... args) {
        return buildWebsocketCommandJson("subscribe", args);
    }

    protected String buildUnSubscribeCommand(String... args) {
        return buildWebsocketCommandJson("unsubscribe", args);
    }

    protected <T> BitmexResponse<T> parseMessage(String message, TypeToken<BitmexResponse<T>> type) {
        return gson.fromJson(message, type.getType());
    }

    protected void initMapping(MappingFunction[] mappingFunctions) {
        this.mappingFunctions = mappingFunctions;
    }

    protected MappingFunction resolveMappingFunction(String table) {
        if (StringUtils.isEmpty(table)) {
            throw new IllegalArgumentException("Invalid table name");
        }

        for (MappingFunction mappingFunction: mappingFunctions) {
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

                if (success != null) {
                    if (success.getAsBoolean()) {
                        JsonElement subscribe = element.getAsJsonObject().get("subscribe");
                        if (subscribe != null) {
                            resolveMappingFunction(subscribe.getAsString()).setEnabled(true);
                        }
                    }
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
