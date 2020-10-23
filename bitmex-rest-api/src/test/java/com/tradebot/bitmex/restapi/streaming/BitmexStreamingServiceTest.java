package com.tradebot.bitmex.restapi.streaming;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.BitmexTestConstants;
import com.tradebot.bitmex.restapi.BitmexTestUtils;
import com.tradebot.bitmex.restapi.events.OrderEventPayLoad;
import com.tradebot.bitmex.restapi.events.TradeEventPayLoad;
import com.tradebot.bitmex.restapi.streaming.events.BitmexEventsStreamingService;
import com.tradebot.bitmex.restapi.streaming.marketdata.BitmexMarketDataStreamingService;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.events.EventCallback;
import com.tradebot.core.events.EventCallbackImpl;
import com.tradebot.core.heartbeats.HeartBeatCallback;
import com.tradebot.core.heartbeats.HeartBeatCallbackImpl;
import com.tradebot.core.heartbeats.HeartBeatPayLoad;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.MarketDataPayLoad;
import com.tradebot.core.marketdata.MarketEventCallback;
import com.tradebot.core.marketdata.MarketEventHandlerImpl;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;


public class BitmexStreamingServiceTest {

    private volatile int audcadCt;
    private volatile int nzdsgdCt;
    private volatile int tradeEventCt;
    private volatile int orderEventCt;
    private volatile int heartbeatCt;
    private static final int expectedPriceEvents = 668;// 1 for each
    // private static final Long anotherAccId = 234567L;
    private static final TradeableInstrument<String> AUDCAD = new TradeableInstrument<>("AUD_CAD");
    private static final TradeableInstrument<String> NZDSGD = new TradeableInstrument<>("NZD_SGD");
    private AtomicReference<MarketDataPayLoad<String>> audcadLastRef = new AtomicReference<>();
    private AtomicReference<MarketDataPayLoad<String>> nzdsgdLastRef = new AtomicReference<>();
    private static final String disconnectmsg = "{\"disconnect\":{\"code\":64,\"message\":\"bye\",\"moreInfo\":\"none\"}}";

    @Before
    public void reset() {
        heartbeatCt = 0;
    }

    @Test
    public void eventsStreaming() throws Exception {

        @SuppressWarnings("unchecked")
        AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
        Collection<Account<Long>> mockAccounts = getMockAccounts();
        when(accountDataProvider.getLatestAccountsInfo()).thenReturn(mockAccounts);
        EventBus eventBus = new EventBus();
        eventBus.register(this);
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<>(eventBus);
        EventCallback<JSONObject> eventCallback = new EventCallbackImpl<>(eventBus);

        BitmexStreamingService service = new BitmexEventsStreamingService(
            BitmexTestConstants.streaming_url,
            BitmexTestConstants.accessToken, accountDataProvider, eventCallback, heartBeatCallback,
            "TESTEVTSTREAM");
        assertEquals("https://stream-fxtrade.oanda.com/v1/events?accountIds=123456%2C234567",
            service.getStreamingUrl());
        BitmexStreamingService spy = setUpSpy(service, "src/test/resources/events.txt");
        assertEquals(6, heartbeatCt);
        assertEquals(1, this.orderEventCt);
        assertEquals(2, this.tradeEventCt);
        verify(spy, times(1)).handleDisconnect(disconnectmsg);
    }

    @SuppressWarnings("unchecked")
    private Collection<Account<Long>> getMockAccounts() {
        Collection<Account<Long>> mockAccounts = Lists.newArrayListWithExpectedSize(2);
        Account<Long> account1 = mock(Account.class);
        when(account1.getAccountId()).thenReturn(BitmexTestConstants.accountId);
        Account<Long> account2 = mock(Account.class);
        when(account2.getAccountId()).thenReturn(BitmexTestConstants.accountId2);

        mockAccounts.add(account1);
        mockAccounts.add(account2);
        return mockAccounts;
    }

    private BitmexStreamingService setUpSpy(BitmexStreamingService service, String fname)
        throws Exception {
        BitmexStreamingService spy = spy(service);
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(spy.getHttpClient()).thenReturn(mockHttpClient);
        when(spy.isStreaming()).thenReturn(service.isStreaming());
        BitmexTestUtils.mockHttpInteraction(fname, mockHttpClient);
        spy.startStreaming();
        do {
            Thread.sleep(2L);
        } while (spy.streamThread.isAlive());
        return spy;
    }

    @Test
    public void marketDataStreaming() throws Exception {
        Collection<TradeableInstrument<String>> instruments = Lists.newArrayList();
        EventBus eventBus = new EventBus();
        MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(
            eventBus);
        eventBus.register(this);
        instruments.add(AUDCAD);
        instruments.add(NZDSGD);
        BitmexStreamingService service = new BitmexMarketDataStreamingService(
            BitmexTestConstants.streaming_url,
            BitmexTestConstants.accessToken, BitmexTestConstants.accountId, instruments,
            mktEventCallback,
            heartBeatCallback, "TESTMKTSTREAM");
        assertEquals(
            "https://stream-fxtrade.oanda.com/v1/prices?accountId=123456&instruments=AUD_CAD%2CNZD_SGD",
            service.getStreamingUrl());
        BitmexStreamingService spy = setUpSpy(service, "src/test/resources/marketData123456.txt");
        assertEquals(expectedPriceEvents / 2, audcadCt);
        assertEquals(expectedPriceEvents / 2, nzdsgdCt);
        assertEquals(expectedPriceEvents / 4, heartbeatCt);
        MarketDataPayLoad<String> audcadPayLoad = audcadLastRef.get();
        assertEquals(1.0149, audcadPayLoad.getBidPrice(), BitmexTestConstants.precision);
        assertEquals(1.0151, audcadPayLoad.getAskPrice(), BitmexTestConstants.precision);
        assertEquals(1401920421958L, audcadPayLoad.getEventDate().getMillis());
        MarketDataPayLoad<String> nzdsgdPayLoad = nzdsgdLastRef.get();
        assertEquals(1.0799, nzdsgdPayLoad.getBidPrice(), BitmexTestConstants.precision);
        assertEquals(1.0801, nzdsgdPayLoad.getAskPrice(), BitmexTestConstants.precision);
        assertEquals(1401920421958L, nzdsgdPayLoad.getEventDate().getMillis());
        verify(spy, times(1)).handleDisconnect(disconnectmsg);
    }

    @Subscribe
    public void dummyMarketDataSubscriber(MarketDataPayLoad<String> payLoad) {
        if (payLoad.getInstrument().equals(AUDCAD)) {
            this.audcadCt++;
            this.audcadLastRef.set(payLoad);
        } else {
            this.nzdsgdCt++;
            this.nzdsgdLastRef.set(payLoad);
        }
    }

    @Subscribe
    public void dummyTradeEventSubscriber(TradeEventPayLoad payLoad) {
        this.tradeEventCt++;
    }

    @Subscribe
    public void dummyOrderEventSubscriber(OrderEventPayLoad payLoad) {
        this.orderEventCt++;
    }

    @Subscribe
    public void dummyHeartBeatSubscriber(HeartBeatPayLoad<DateTime> payLoad) {
        heartbeatCt++;
    }
}
