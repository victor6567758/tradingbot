
package com.tradebot.bitmex.restapi.streaming;

import static com.tradebot.bitmex.restapi.BitmexJsonKeys.heartbeat;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.time;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatPayLoad;
import com.tradebot.core.streaming.heartbeats.HeartBeatStreamingService;
import com.tradebot.core.utils.TradingUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

@Slf4j
public abstract class BitmexStreamingService implements HeartBeatStreamingService {

    protected volatile boolean serviceUp = true;
    private final HeartBeatCallback<DateTime> heartBeatCallback;
    private final String hearbeatSourceId;
    protected Thread streamThread;
    private final BasicHeader authHeader;

    protected abstract String getStreamingUrl();

    protected abstract void startStreaming();

    protected abstract void stopStreaming();

    protected CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    protected BitmexStreamingService(String accessToken,
        HeartBeatCallback<DateTime> heartBeatCallback,
        String heartbeatSourceId) {
        this.hearbeatSourceId = heartbeatSourceId;
        this.heartBeatCallback = heartBeatCallback;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    protected void handleHeartBeat(JSONObject streamEvent) {
        long t = Long.parseLong(((JSONObject) streamEvent.get(heartbeat)).get(time).toString());
        heartBeatCallback.onHeartBeat(
            new HeartBeatPayLoad<>(new DateTime(TradingUtils.toMillisFromNanos(t)),
                hearbeatSourceId));
    }

    protected BufferedReader setUpStreamIfPossible(CloseableHttpClient httpClient)
        throws Exception {
        HttpUriRequest httpGet = new HttpGet(getStreamingUrl());
        httpGet.setHeader(authHeader);
        httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);
        log.info(TradingUtils.executingRequestMsg(httpGet));
        HttpResponse resp = httpClient.execute(httpGet);
        HttpEntity entity = resp.getEntity();
        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
            InputStream stream = entity.getContent();
            serviceUp = true;
            return new BufferedReader(new InputStreamReader(stream));
        } else {
            String responseString = EntityUtils.toString(entity, "UTF-8");
            log.warn(responseString);
            return null;
        }
    }

    protected void handleDisconnect(String line) {
        serviceUp = false;
        log.warn(String.format("Disconnect message received for stream %s. PayLoad->%s",
            getHeartBeatSourceId(), line));
    }

    protected boolean isStreaming() {
        return serviceUp;
    }

    @Override
    public void stopHeartBeatStreaming() {
        stopStreaming();
    }

    @Override
    public void startHeartBeatStreaming() {
        startStreaming();
    }

    @Override
    public String getHeartBeatSourceId() {
        return this.hearbeatSourceId;
    }
}
