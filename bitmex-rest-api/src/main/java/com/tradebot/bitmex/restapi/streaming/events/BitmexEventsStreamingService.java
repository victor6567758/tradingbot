package com.tradebot.bitmex.restapi.streaming.events;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.streaming.BitmexStreamingService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.streaming.events.EventsStreamingService;
import java.io.BufferedReader;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class BitmexEventsStreamingService extends BitmexStreamingService implements
    EventsStreamingService {

    private final String url;
    private final AccountDataProvider<Long> accountDataProvider;
    private final EventCallback<JSONObject> eventCallback;

    public BitmexEventsStreamingService(final String url, final String accessToken,
        AccountDataProvider<Long> accountDataProvider, EventCallback<JSONObject> eventCallback,
        HeartBeatCallback<DateTime> heartBeatCallback, String heartBeatSourceId) {
        super(accessToken, heartBeatCallback, heartBeatSourceId);
        this.url = url;
        this.accountDataProvider = accountDataProvider;
        this.eventCallback = eventCallback;
    }

    @Override
    public void stopEventsStreaming() {
        this.serviceUp = false;
        if (streamThread != null && streamThread.isAlive()) {
            streamThread.interrupt();
        }
    }

    private String accountsAsCsvString(Collection<Account<Long>> accounts) {
        StringBuilder accountsAsCsv = new StringBuilder();
        boolean firstTime = true;
        for (Account<Long> account : accounts) {
            if (firstTime) {
                firstTime = false;
            } else {
                accountsAsCsv.append(TradingConstants.ENCODED_COMMA);
            }
            accountsAsCsv.append(account.getAccountId());
        }
        return accountsAsCsv.toString();
    }

    @Override
    protected String getStreamingUrl() {
        Collection<Account<Long>> accounts = accountDataProvider.getLatestAccountsInfo();
        return this.url + BitmexConstants.EVENTS_RESOURCE + "?accountIds=" + accountsAsCsvString(
            accounts);
    }

    @Override
    public void startEventsStreaming() {
        stopEventsStreaming();
        streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try (CloseableHttpClient httpClient = getHttpClient()) {
                    BufferedReader br = setUpStreamIfPossible(httpClient);
                    if (br != null) {
                        String line;
                        while ((line = br.readLine()) != null && serviceUp) {
                            Object obj = JSONValue.parse(line);
                            JSONObject jsonPayLoad = (JSONObject) obj;
                            if (jsonPayLoad.containsKey(BitmexJsonKeys.heartbeat)) {
                                handleHeartBeat(jsonPayLoad);
                            } else if (jsonPayLoad.containsKey(BitmexJsonKeys.transaction)) {
                                JSONObject transactionObject = (JSONObject) jsonPayLoad
                                    .get(BitmexJsonKeys.transaction);
                                String transactionType = transactionObject.get(BitmexJsonKeys.type)
                                    .toString();
                                /*convert here so that event bus can post to an appropriate handler,
                                 * event though this does not belong here*/
                                EventPayLoad<JSONObject> payLoad = BitmexUtils
                                    .toBitmexEventPayLoad(transactionType,
                                        transactionObject);
                                if (payLoad != null) {
                                    eventCallback.onEvent(payLoad);
                                }
                            } else if (jsonPayLoad.containsKey(BitmexJsonKeys.disconnect)) {
                                handleDisconnect(line);
                            }
                        }
                        br.close();
                    }

                } catch (Exception e) {
                    log.error("error encountered inside event streaming thread", e);
                } finally {
                    serviceUp = false;

                }

            }
        }, "OandEventStreamingThread");
        streamThread.start();
    }

    @Override
    protected void startStreaming() {
        this.startEventsStreaming();
    }

    @Override
    protected void stopStreaming() {
        this.stopEventsStreaming();
    }

}
