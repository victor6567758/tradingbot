package com.tradebot.bitmex.restapi.streaming.marketdata;

import com.google.common.reflect.TypeToken;
import com.tradebot.bitmex.restapi.model.websocket.BitmexQuote;
import com.tradebot.bitmex.restapi.model.websocket.BitmexResponse;
import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService2;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;

@Slf4j
public class BitmexMarketDataStreamingService2 extends BaseBitmexStreamingService2 implements MarketDataStreamingService {

    private static final String QUOTE = "quote";
    private static final String INSTRUMENT = "instrument";

    public BitmexMarketDataStreamingService2(
        MarketEventCallback<String> marketEventCallback,
        HeartBeatCallback<DateTime> heartBeatCallback,
        Collection<TradeableInstrument<String>> instruments) {
        super(heartBeatCallback);

        this.marketEventCallback = marketEventCallback;
        this.instruments = instruments;

        MappingFunction[] instrumentMappingArray = instruments.stream()
            .map(instrument -> new MappingFunction(this::processInstrumentReply, getInstrumentParameters(instrument.getInstrument())))
            .toArray(MappingFunction[]::new);

        MappingFunction[] quoteMappingArray = instruments.stream()
            .map(instrument -> new MappingFunction(this::processQuoteReply, getQuoteParameters(instrument.getInstrument())))
            .toArray(MappingFunction[]::new);

        initMapping(ArrayUtils.addAll(
            instrumentMappingArray,
            ArrayUtils.addAll(quoteMappingArray,
                new MappingFunction(this::processInstrumentReply, INSTRUMENT), new MappingFunction(this::processQuoteReply, QUOTE))
            )
        );

    }

    private final MarketEventCallback<String> marketEventCallback;
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
    protected void connect() {
        for (TradeableInstrument<String> instrument : instruments) {
            log.info("Subscribed to: {}", instrument.getInstrument());
            jettyCommunicationSocket.subscribe(buildSubscribeCommand(getInstrumentParameters(instrument.getInstrument())));
            jettyCommunicationSocket.subscribe(buildSubscribeCommand(getQuoteParameters(instrument.getInstrument())));
        }
    }

    @Override
    protected void disconnect() {
        for (TradeableInstrument<String> instrument : instruments) {
            log.info("Unsubscribed from: {}", instrument.getInstrument());
            jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(getInstrumentParameters(instrument.getInstrument())));
            jettyCommunicationSocket.subscribe(buildUnSubscribeCommand(getQuoteParameters(instrument.getInstrument())));
        }
    }

    private void processQuoteReply(String message) {

        BitmexResponse<BitmexQuote> quotes = parseMessage(message, new TypeToken<>() {
        });

        for (BitmexQuote quote : quotes.getData()) {
            marketEventCallback.onMarketEvent(
                new TradeableInstrument<>(quote.getSymbol()),
                quote.getBidPrice(),
                quote.getAskPrice(),
                quote.getTimestamp()
            );

            if (log.isDebugEnabled()) {
                log.debug("parsed market event: {}", quote.getSymbol());
            }
        }
    }

    private void processInstrumentReply(String message) {
        if (log.isDebugEnabled()) {
            log.debug("parsed instrument event: {}", message);
        }
    }


    private static String getInstrumentParameters(String instrumentName) {
        return INSTRUMENT + ":" + instrumentName;
    }

    private static String getQuoteParameters(String instrumentName) {
        return QUOTE + ":" + instrumentName;
    }


}
