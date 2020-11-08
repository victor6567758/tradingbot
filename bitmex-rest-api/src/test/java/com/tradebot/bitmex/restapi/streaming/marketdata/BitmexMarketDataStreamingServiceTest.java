package com.tradebot.bitmex.restapi.streaming.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tradebot.bitmex.restapi.model.BitmexInstrument;
import com.tradebot.bitmex.restapi.model.BitmexQuote;
import com.tradebot.bitmex.restapi.model.BitmexResponse;
import com.tradebot.bitmex.restapi.streaming.JettyCommunicationSocket;
import com.tradebot.bitmex.restapi.utils.BitmexJsonBuilder;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatPayLoad;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketEventCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.data.Offset;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class BitmexMarketDataStreamingServiceTest {

    private static final Collection<TradeableInstrument<String>> INSTRUMENTS = Arrays.asList(
        new TradeableInstrument<>("XBTUSD"),
        new TradeableInstrument<>("XBTJPY")
    );

    private static final TradeableInstrument INSTRUMENTS_XBTUSD = INSTRUMENTS.stream().filter(n -> "XBTUSD".equals(n.getInstrument())).findAny().orElseThrow();
    private static final TradeableInstrument INSTRUMENTS_XBTJPY = INSTRUMENTS.stream().filter(n -> "XBTJPY".equals(n.getInstrument())).findAny().orElseThrow();


    private static final Gson GSON = BitmexJsonBuilder.buildJson();


    private JettyCommunicationSocket jettyCommunicationSocketSpy;
    private BitmexMarketDataStreamingService bitmexMarketDataStreamingService2Spy;

    private String instrumentXBTJPY;
    private String instrumentXBTUSD;

    private String instrumentXBTJPYSubscribe;
    private String instrumentXBTUSDSubscribe;

    private String quoteXBTJPYSubscribe;
    private String quoteXBTUSDSubscribe;

    private String quoteXBTJPY;
    private String quoteXBTUSD;

    private List<BitmexQuote> quotesXBTJPY;
    private List<BitmexQuote> quotesXBTUSD;

    private List<BitmexInstrument> instrumentsXBTJPY;
    private List<BitmexInstrument> instrumentsXBTUSD;


    private HeartBeatCallback<Long> heartBeatCallbackSpy;
    private EventCallback<BitmexInstrument> bitmexInstrumentEventCallbackSpy;
    private MarketEventCallback<String> marketEventCallbackSpy;

    // Must not be lambdas for correct Mockito work
    private final HeartBeatCallback<Long> heartBeatCallback = new HeartBeatCallback<Long>() {

        @Override
        public void onHeartBeat(HeartBeatPayLoad<Long> payLoad) {
        }
    };
    private final EventCallback<BitmexInstrument> bitmexInstrumentEventCallback = new EventCallback<BitmexInstrument>() {

        @Override
        public void onEvent(EventPayLoad<BitmexInstrument> eventPayLoad) {

        }
    };
    private final MarketEventCallback<String> marketEventCallback = new MarketEventCallback<String>() {

        @Override
        public void onMarketEvent(TradeableInstrument<String> instrument, double bid, double ask, DateTime eventDate) {

        }
    };

    @Before
    public void init() throws IOException {

        instrumentXBTJPY = Resources.toString(Resources.getResource("webSocketInstrumentXBTJPY.json"), StandardCharsets.UTF_8);
        instrumentXBTUSD = Resources.toString(Resources.getResource("webSocketInstrumentXBTUSD.json"), StandardCharsets.UTF_8);

        assertThat(instrumentXBTJPY).isNotBlank();
        assertThat(instrumentXBTUSD).isNotBlank();

        quoteXBTJPY = Resources.toString(Resources.getResource("webSocketQuoteXBTJPY.json"), StandardCharsets.UTF_8);
        quoteXBTUSD = Resources.toString(Resources.getResource("webSocketQuoteXBTUSD.json"), StandardCharsets.UTF_8);

        assertThat(quoteXBTJPY).isNotBlank();
        assertThat(quoteXBTUSD).isNotBlank();

        instrumentXBTJPYSubscribe = Resources.toString(Resources.getResource("subscribeWebSocketInstrumentXBTJPY.json"), StandardCharsets.UTF_8);
        instrumentXBTUSDSubscribe = Resources.toString(Resources.getResource("subscribeWebSocketInstrumentXBTUSD.json"), StandardCharsets.UTF_8);

        assertThat(instrumentXBTJPYSubscribe).isNotBlank();
        assertThat(instrumentXBTUSDSubscribe).isNotBlank();

        quoteXBTJPYSubscribe = Resources.toString(Resources.getResource("subscribeWebSocketQuoteXBTJPY.json"), StandardCharsets.UTF_8);
        quoteXBTUSDSubscribe = Resources.toString(Resources.getResource("subscribeWebSocketQuoteXBTUSD.json"), StandardCharsets.UTF_8);

        assertThat(quoteXBTJPYSubscribe).isNotBlank();
        assertThat(quoteXBTUSDSubscribe).isNotBlank();

        TypeToken<BitmexResponse<BitmexQuote>> typeTokenQuote = new TypeToken<>() {
        };
        TypeToken<BitmexResponse<BitmexInstrument>> typeTokenInstrument = new TypeToken<>() {
        };

        quotesXBTJPY = Arrays.stream(GSON.<BitmexResponse<BitmexQuote>>fromJson(quoteXBTJPY, typeTokenQuote.getType()).getData())
            .collect(Collectors.toList());
        quotesXBTUSD = Arrays.stream(GSON.<BitmexResponse<BitmexQuote>>fromJson(quoteXBTUSD, typeTokenQuote.getType()).getData())
            .collect(Collectors.toList());

        instrumentsXBTJPY = Arrays.stream(GSON.<BitmexResponse<BitmexInstrument>>fromJson(instrumentXBTJPY, typeTokenInstrument.getType()).getData())
            .collect(Collectors.toList());
        instrumentsXBTUSD = Arrays.stream(GSON.<BitmexResponse<BitmexInstrument>>fromJson(instrumentXBTUSD, typeTokenInstrument.getType()).getData())
            .collect(Collectors.toList());

        assertThat(quotesXBTJPY).isNotEmpty();
        assertThat(quotesXBTUSD).isNotEmpty();
        assertThat(instrumentsXBTJPY).isNotEmpty();
        assertThat(instrumentsXBTUSD).isNotEmpty();

        heartBeatCallbackSpy = spy(heartBeatCallback);
        bitmexInstrumentEventCallbackSpy = spy(bitmexInstrumentEventCallback);
        marketEventCallbackSpy = spy(marketEventCallback);

        bitmexMarketDataStreamingService2Spy = spy(new BitmexMarketDataStreamingService(
            marketEventCallbackSpy,
            bitmexInstrumentEventCallbackSpy,
            heartBeatCallbackSpy,
            INSTRUMENTS
        ));
        jettyCommunicationSocketSpy = spy(bitmexMarketDataStreamingService2Spy.getJettyCommunicationSocket());

        doNothing().when(bitmexMarketDataStreamingService2Spy).shutdown();
        doNothing().when(bitmexMarketDataStreamingService2Spy).init();
        doNothing().when(bitmexMarketDataStreamingService2Spy).startMarketDataStreaming();
        doNothing().when(bitmexMarketDataStreamingService2Spy).stopMarketDataStreaming();

        bitmexMarketDataStreamingService2Spy.init();
        bitmexMarketDataStreamingService2Spy.startMarketDataStreaming();
    }

    @After
    public void tearDown() {
        bitmexMarketDataStreamingService2Spy.stopMarketDataStreaming();
        bitmexMarketDataStreamingService2Spy.shutdown();
    }

    @Test
    public void testMarkedDataXBTJPY() {

        ArgumentCaptor<TradeableInstrument<String>> argument1Captor = ArgumentCaptor.forClass(TradeableInstrument.class);
        ArgumentCaptor<Double> argument2Captor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> argument3Captor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<DateTime> argument4Captor = ArgumentCaptor.forClass(DateTime.class);

        jettyCommunicationSocketSpy.onMessage(quoteXBTJPYSubscribe);
        jettyCommunicationSocketSpy.onMessage(quoteXBTJPY);

        verify(marketEventCallbackSpy, times(2)).onMarketEvent(argument1Captor.capture(), argument2Captor.capture(), argument3Captor.capture(), argument4Captor.capture());

        List<TradeableInstrument<String>> capturedArgument1List = argument1Captor.getAllValues();
        List<Double> capturedArgument2List = argument2Captor.getAllValues();
        List<Double> capturedArgument3List = argument3Captor.getAllValues();
        List<DateTime> capturedArgument4List = argument4Captor.getAllValues();

        assertThat(capturedArgument1List.get(0).getInstrument()).isEqualTo(instrumentsXBTJPY.get(0).getSymbol());
        assertThat(capturedArgument2List.get(0)).isCloseTo(quotesXBTJPY.get(0).getBidPrice(), Offset.offset(0.0001));
        assertThat(capturedArgument3List.get(0)).isCloseTo(quotesXBTJPY.get(0).getAskPrice(), Offset.offset(0.0001));
        assertThat(capturedArgument4List.get(0)).isEqualTo(quotesXBTJPY.get(0).getTimestamp());

        assertThat(capturedArgument2List.get(1)).isCloseTo(quotesXBTJPY.get(1).getBidPrice(), Offset.offset(0.0001));
        assertThat(capturedArgument3List.get(1)).isCloseTo(quotesXBTJPY.get(1).getAskPrice(), Offset.offset(0.0001));
        assertThat(capturedArgument4List.get(1)).isEqualTo(quotesXBTJPY.get(1).getTimestamp());

    }

    @Test
    public void testMarkedDataXBTUSD() {

        ArgumentCaptor<TradeableInstrument<String>> argument1Captor = ArgumentCaptor.forClass(TradeableInstrument.class);
        ArgumentCaptor<Double> argument2Captor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> argument3Captor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<DateTime> argument4Captor = ArgumentCaptor.forClass(DateTime.class);

        jettyCommunicationSocketSpy.onMessage(quoteXBTUSDSubscribe);
        jettyCommunicationSocketSpy.onMessage(quoteXBTUSD);

        verify(marketEventCallbackSpy, times(1)).onMarketEvent(argument1Captor.capture(), argument2Captor.capture(), argument3Captor.capture(), argument4Captor.capture());

        TradeableInstrument<String> capturedArgument1 = argument1Captor.getValue();
        Double capturedArgument2 = argument2Captor.getValue();
        Double capturedArgument3 = argument3Captor.getValue();
        DateTime capturedArgument4 = argument4Captor.getValue();

        assertThat(capturedArgument1.getInstrument()).isEqualTo(instrumentsXBTUSD.get(0).getSymbol());
        assertThat(capturedArgument2).isCloseTo(quotesXBTUSD.get(0).getBidPrice(), Offset.offset(0.0001));
        assertThat(capturedArgument3).isCloseTo(quotesXBTUSD.get(0).getAskPrice(), Offset.offset(0.0001));
        assertThat(capturedArgument4).isEqualTo(quotesXBTUSD.get(0).getTimestamp());

    }

    @Test
    public void testInstrumentsCallbackXBTJPY() {
        ArgumentCaptor<EventPayLoad<BitmexInstrument>> argumentCaptor = ArgumentCaptor.forClass(EventPayLoad.class);

        jettyCommunicationSocketSpy.onMessage(instrumentXBTJPYSubscribe);
        jettyCommunicationSocketSpy.onMessage(instrumentXBTJPY);
        verify(bitmexInstrumentEventCallbackSpy, times(1)).onEvent(argumentCaptor.capture());
        EventPayLoad<BitmexInstrument> capturedArgument = argumentCaptor.getValue();

        assertThat(capturedArgument.getPayLoad().getSymbol()).isEqualTo(INSTRUMENTS_XBTJPY.getInstrument());
        assertThat(capturedArgument.getPayLoad().getFundingTimestamp()).isEqualTo(instrumentsXBTJPY.get(0).getFundingTimestamp());
        assertThat(capturedArgument.getPayLoad().getFundingRate()).isCloseTo(instrumentsXBTJPY.get(0).getFundingRate(), Offset.offset(0.0001));
        assertThat(capturedArgument.getPayLoad().getIndicativeFundingRate()).isCloseTo(instrumentsXBTJPY.get(0).getIndicativeFundingRate(), Offset.offset(0.0001));
    }


    @Test
    public void testInstrumentsCallbackXBTUSD() {
        ArgumentCaptor<EventPayLoad<BitmexInstrument>> argumentCaptor = ArgumentCaptor.forClass(EventPayLoad.class);

        jettyCommunicationSocketSpy.onMessage(instrumentXBTJPYSubscribe);
        jettyCommunicationSocketSpy.onMessage(instrumentXBTUSD);
        verify(bitmexInstrumentEventCallbackSpy).onEvent(argumentCaptor.capture());
        EventPayLoad<BitmexInstrument> capturedArgument = argumentCaptor.getValue();

        assertThat(capturedArgument.getPayLoad().getSymbol()).isEqualTo(INSTRUMENTS_XBTUSD.getInstrument());
        assertThat(capturedArgument.getPayLoad().getFundingTimestamp()).isEqualTo(instrumentsXBTUSD.get(0).getFundingTimestamp());
        assertThat(capturedArgument.getPayLoad().getFundingRate()).isCloseTo(instrumentsXBTUSD.get(0).getFundingRate(), Offset.offset(0.0001));
        assertThat(capturedArgument.getPayLoad().getIndicativeFundingRate()).isCloseTo(instrumentsXBTUSD.get(0).getIndicativeFundingRate(), Offset.offset(0.0001));

    }

    @Test
    public void testHeartBeat() {
        jettyCommunicationSocketSpy.onMessage("pong");
        verify(heartBeatCallbackSpy, times(1)).onHeartBeat(any(HeartBeatPayLoad.class));
    }


}