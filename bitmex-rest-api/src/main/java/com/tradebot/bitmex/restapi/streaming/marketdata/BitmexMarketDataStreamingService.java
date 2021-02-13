package com.tradebot.bitmex.restapi.streaming.marketdata;

import com.google.common.reflect.TypeToken;
import com.tradebot.bitmex.restapi.events.payload.BitmexInstrumentEventPayload;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.model.BitmexInstrument;
import com.tradebot.bitmex.restapi.model.BitmexQuote;
import com.tradebot.bitmex.restapi.model.BitmexResponse;
import com.tradebot.bitmex.restapi.streaming.BaseBitmexStreamingService;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.streaming.marketdata.MarketDataStreamingService;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class BitmexMarketDataStreamingService extends BaseBitmexStreamingService implements MarketDataStreamingService {

    private static final String QUOTE = "quote";
    private static final String INSTRUMENT = "instrument";
    private static final char QUOTE_DELIMITER = ':';

    private final InstrumentService instrumentService;

    public BitmexMarketDataStreamingService(
        MarketEventCallback marketEventCallback,
        EventCallback<BitmexInstrument> instrumentEventCallback,
        HeartBeatCallback<Long> heartBeatCallback,
        Collection<TradeableInstrument> instruments,
        InstrumentService instrumentService) {
        super(heartBeatCallback);

        this.marketEventCallback = marketEventCallback;
        this.instrumentEventCallback = instrumentEventCallback;
        this.instruments = instruments;
        this.instrumentService = instrumentService;

        initMapping(
            new MappingFunction[]{
                new MappingFunction(this::processInstrumentReply, INSTRUMENT),
                new MappingFunction(this::processQuoteReply, QUOTE)
            }
        );
    }

    private final MarketEventCallback marketEventCallback;
    private final EventCallback<BitmexInstrument> instrumentEventCallback;
    private final Collection<TradeableInstrument> instruments;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

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
        for (TradeableInstrument instrument : instruments) {
            log.info("Subscribed to: {}", instrument.getInstrument());
            jettyCommunicationSocket.subscribe(buildSubscribeCommand(getInstrumentParameters(instrument.getInstrument())));
            jettyCommunicationSocket.subscribe(buildSubscribeCommand(getQuoteParameters(instrument.getInstrument())));
        }
    }

    @Override
    public void stopMarketDataStreaming() {
        for (TradeableInstrument instrument : instruments) {
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
                instrumentService.resolveTradeableInstrument(quote.getSymbol()),
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
            instrumentEventCallback.onEvent(new BitmexInstrumentEventPayload(TradeEvents.EVENT_INSTRUMENT, bitmexInstrument));

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
