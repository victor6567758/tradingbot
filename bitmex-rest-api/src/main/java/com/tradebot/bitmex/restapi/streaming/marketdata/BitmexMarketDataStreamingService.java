package com.tradebot.bitmex.restapi.streaming.marketdata;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.streaming.BitmexStreamingService;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import com.tradebot.core.utils.TradingUtils;
import java.io.BufferedReader;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


@Slf4j
public class BitmexMarketDataStreamingService extends BitmexStreamingService implements
    MarketDataStreamingService {


    private final String url;
    private final MarketEventCallback<String> marketEventCallback;

    public BitmexMarketDataStreamingService(String url, String accessToken, long accountId,
        Collection<TradeableInstrument<String>> instruments,
        MarketEventCallback<String> marketEventCallback,
        HeartBeatCallback<DateTime> heartBeatCallback, String heartbeatSourceId) {
        super(accessToken, heartBeatCallback, heartbeatSourceId);
        this.url =
            url + BitmexConstants.PRICES_RESOURCE + "?accountId=" + accountId + "&instruments="
                + instrumentsAsCsv(instruments);
        this.marketEventCallback = marketEventCallback;
    }

    private String instrumentsAsCsv(Collection<TradeableInstrument<String>> instruments) {
        StringBuilder csvLst = new StringBuilder();
        boolean firstTime = true;
        for (TradeableInstrument<String> instrument : instruments) {
            if (firstTime) {
                firstTime = false;
            } else {
                csvLst.append(TradingConstants.ENCODED_COMMA);
            }
            csvLst.append(instrument.getInstrument());
        }
        return csvLst.toString();
    }

    @Override
    protected String getStreamingUrl() {
        return this.url;
    }

    @Override
    public void stopMarketDataStreaming() {
        this.serviceUp = false;
        if (streamThread != null && streamThread.isAlive()) {
            streamThread.interrupt();
        }
    }

    @Override
    public void startMarketDataStreaming() {
        stopMarketDataStreaming();
        this.streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try (CloseableHttpClient httpClient = getHttpClient()) {
                    BufferedReader br = setUpStreamIfPossible(httpClient);
                    if (br != null) {
                        String line;
                        while ((line = br.readLine()) != null && serviceUp) {
                            Object obj = JSONValue.parse(line);
                            JSONObject instrumentTick = (JSONObject) obj;
                            // unwrap if necessary
                            if (instrumentTick.containsKey(BitmexJsonKeys.tick)) {
                                instrumentTick = (JSONObject) instrumentTick
                                    .get(BitmexJsonKeys.tick);
                            }

                            if (instrumentTick.containsKey(BitmexJsonKeys.instrument)) {
                                final String instrument = instrumentTick
                                    .get(BitmexJsonKeys.instrument).toString();
                                final String timeAsString = instrumentTick.get(BitmexJsonKeys.time)
                                    .toString();
                                final long eventTime = Long.parseLong(timeAsString);
                                final double bidPrice = ((Number) instrumentTick.get(
                                    BitmexJsonKeys.bid)).doubleValue();
                                final double askPrice = ((Number) instrumentTick.get(
                                    BitmexJsonKeys.ask)).doubleValue();
                                marketEventCallback
                                    .onMarketEvent(new TradeableInstrument<String>(instrument),
                                        bidPrice, askPrice,
                                        new DateTime(TradingUtils.toMillisFromNanos(eventTime)));
                            } else if (instrumentTick.containsKey(BitmexJsonKeys.heartbeat)) {
                                handleHeartBeat(instrumentTick);
                            } else if (instrumentTick.containsKey(BitmexJsonKeys.disconnect)) {
                                handleDisconnect(line);
                            }
                        }
                        br.close();
                        // stream.close();
                    }
                } catch (Exception e) {
                    log.error("error encountered inside market data streaming thread", e);
                } finally {
                    serviceUp = false;
                }

            }
        }, "OandMarketDataStreamingThread");
        this.streamThread.start();

    }

    @Override
    protected void startStreaming() {
        startMarketDataStreaming();

    }

    @Override
    protected void stopStreaming() {
        stopMarketDataStreaming();

    }

}
