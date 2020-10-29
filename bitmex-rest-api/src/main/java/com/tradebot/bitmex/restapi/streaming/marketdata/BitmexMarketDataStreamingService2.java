package com.tradebot.bitmex.restapi.streaming.marketdata;

import com.google.common.reflect.TypeToken;
import com.tradebot.bitmex.restapi.events.BitmexInstrumentEventPayload;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.model.websocket.BitmexInstrument;
import com.tradebot.bitmex.restapi.model.websocket.BitmexQuote;
import com.tradebot.bitmex.restapi.model.websocket.BitmexResponse;
import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService2;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class BitmexMarketDataStreamingService2 extends BaseBitmexStreamingService2 implements MarketDataStreamingService {

    private static final String QUOTE = "quote";
    private static final String INSTRUMENT = "instrument";
    public static final char QUOTE_DELIMITER = ':';

    public BitmexMarketDataStreamingService2(
        MarketEventCallback<String> marketEventCallback,
        EventCallback<BitmexInstrument> instrumentEventCallback,
        HeartBeatCallback<Long> heartBeatCallback,
        Collection<TradeableInstrument<String>> instruments) {
        super(heartBeatCallback);

        this.marketEventCallback = marketEventCallback;
        this.instrumentEventCallback = instrumentEventCallback;
        this.instruments = instruments;

        initMapping(
            new MappingFunction[]{
                new MappingFunction(this::processInstrumentReply, INSTRUMENT),
                new MappingFunction(this::processQuoteReply, QUOTE)
            }
        );
    }

    private final MarketEventCallback<String> marketEventCallback;
    private final EventCallback<BitmexInstrument> instrumentEventCallback;
    private final Collection<TradeableInstrument<String>> instruments;

    @Override
    protected String extractSubscribeTopic(String subscribeElement) {
        int idx = StringUtils.indexOf(subscribeElement, ':');
        if (idx >= 0) {
            return StringUtils.substring(subscribeElement, 0, idx);
        }
        throw new IllegalArgumentException("Cannot extract subscribe topic");
    }

    @Override
    public void startMarketDataStreaming() {
        for (TradeableInstrument<String> instrument : instruments) {
            log.info("Subscribed to: {}", instrument.getInstrument());
            jettyCommunicationSocket.subscribe(buildSubscribeCommand(getInstrumentParameters(instrument.getInstrument())));
            jettyCommunicationSocket.subscribe(buildSubscribeCommand(getQuoteParameters(instrument.getInstrument())));
        }
    }

    @Override
    public void stopMarketDataStreaming() {
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
                log.debug("Parsed market event: {}", quote.getSymbol());
            }
        }
    }

    private void processInstrumentReply(String message) {

        BitmexResponse<BitmexInstrument> instrument = parseMessage(message, new TypeToken<>() {
        });

        for (BitmexInstrument bitmexInstrument : instrument.getData()) {
            instrumentEventCallback.onEvent(new BitmexInstrumentEventPayload(TradeEvents.BITMEX_INSTRUMENT, bitmexInstrument));

            if (log.isDebugEnabled()) {
                log.debug("Parsed instrument event: {}", bitmexInstrument.toString());
            }
        }


    }


    private static String getInstrumentParameters(String instrumentName) {
        return INSTRUMENT + QUOTE_DELIMITER + instrumentName;
    }

    private static String getQuoteParameters(String instrumentName) {
        return QUOTE + QUOTE_DELIMITER + instrumentName;
    }


}
