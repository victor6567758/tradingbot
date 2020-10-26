package com.tradebot.bitmex.restapi.streaming.marketdata;

import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService2;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

@Slf4j
@RequiredArgsConstructor
public class BaseBitmexMarketDataStreamingService2 extends BaseBitmexStreamingService2 implements MarketDataStreamingService {


    private final MarketEventCallback<String> marketEventCallback;
    private final HeartBeatCallback<DateTime> heartBeatCallback;
    private final Collection<TradeableInstrument<String>> instruments;


    @Override
    public void startMarketDataStreaming() {
        connect();
    }

    @Override
    public void stopMarketDataStreaming() {
        disconnect();
    }

    @Override
    protected void onMessageHandler(String message) {

    }

    @Override
    protected void connect() {
        for (TradeableInstrument<String> instrument : instruments) {
            log.info("Subscribed to: {}", instrument.getInstrument());
            jettyCommunicationSocket.subscribe(buildSubscribeCommand("instrument:" + instrument.getInstrument()));
            jettyCommunicationSocket.subscribe(buildSubscribeCommand("quote:" + instrument.getInstrument()));
        }
    }

    @Override
    protected void disconnect() {
        for (TradeableInstrument<String> instrument : instruments) {
            log.info("Unsubscribed from: {}", instrument.getInstrument());
            jettyCommunicationSocket.subscribe(buildUnSubscribeCommand("instrument:" + instrument.getInstrument()));
            jettyCommunicationSocket.subscribe(buildUnSubscribeCommand("quote:" + instrument.getInstrument()));
        }
    }

    private void processInstrumentReply(String message) {

    }

    private void processQuoteReply(String message) {

    }

}
