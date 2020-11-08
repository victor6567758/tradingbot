package com.tradebot.app;


//import com.google.common.util.concurrent.Uninterruptibles;
//import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
//import com.tradebot.bitmex.restapi.model.BitmexInstrument;
//import com.tradebot.bitmex.restapi.streaming.marketdata.BitmexMarketDataStreamingService2;
//import com.tradebot.core.account.Account;
//import com.tradebot.core.account.AccountDataProvider;
//import com.tradebot.core.events.EventCallback;
//import com.tradebot.core.events.EventPayLoad;
//import com.tradebot.core.heartbeats.HeartBeatCallback;
//import com.tradebot.core.heartbeats.HeartBeatPayLoad;
//import com.tradebot.core.instrument.TradeableInstrument;
//import com.tradebot.core.marketdata.MarketEventCallback;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.concurrent.TimeUnit;
import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.order.BitmexOrderManagementProvider;
import com.tradebot.bitmex.restapi.trade.BitmexTradeManagementProvider;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.order.Order;
import com.tradebot.core.trade.Trade;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
//import org.joda.time.DateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {JmxAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@Slf4j
public class TradeBotApp implements CommandLineRunner {


    public static void main(String[] args) {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(TradeBotApp.class, args);
        log.info("APPLICATION FINISHED");

    }

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line runner");

        AccountDataProvider<Long> accountDataProvider = new BitmexAccountDataProviderService();
        Collection<Account<Long>> accounts = accountDataProvider.getLatestAccountsInfo();

        int kk = 0;
//        BitmexTransactionDataProviderService bitmexTransactionDataProviderService = new BitmexTransactionDataProviderService();
//        Transaction transaction = bitmexTransactionDataProviderService
//            .getTransaction("00000000-0000-0000-0000-000000000000", (Long) accounts.iterator().next().getAccountId());
//
//        BitmexHistoricMarketDataProvider bitmexHistoricMarketDataProvider = new BitmexHistoricMarketDataProvider();
//        List<CandleStick<String>> data = bitmexHistoricMarketDataProvider.getCandleSticks(new TradeableInstrument<>("XBTUSD"),
//            CandleStickGranularity.M1, 100);

//        BitmexCurrentPriceInfoProvider bitmexCurrentPriceInfoProvider = new BitmexCurrentPriceInfoProvider();
//        Price<String> price =  bitmexCurrentPriceInfoProvider.getCurrentPricesForInstrument(new TradeableInstrument<>("XBTUSD"));
//        int t = 0;

//        BitmexPositionManagementProvider bitmexPositionManagementProvider = new BitmexPositionManagementProvider();
//        Position<String> posXbtUsd = bitmexPositionManagementProvider.getPositionForInstrument(accounts.iterator().next().getAccountId(),
//            new TradeableInstrument<>("XBTUSD"));
//
//        Collection<Position<String>> allPositions =
//            bitmexPositionManagementProvider.getPositionsForAccount(accounts.iterator().next().getAccountId());
//
//        int t = 0;

//        MarketEventCallback<String> marketEventCallback = new MarketEventCallback<String>() {
//
//            @Override
//            public void onMarketEvent(TradeableInstrument<String> instrument, double bid, double ask, DateTime eventDate) {
//                log.info("Market data {}, {}, {}", instrument, bid, ask);
//            }
//        };
//
//        EventCallback<BitmexInstrument> instrumentEventCallback = new EventCallback<BitmexInstrument>() {
//
//            @Override
//            public void onEvent(EventPayLoad<BitmexInstrument> eventPayLoad) {
//                log.info("Market instrument event {}", eventPayLoad.getPayLoad().toString());
//            }
//        };
//
//        HeartBeatCallback<Long> heartBeatCallback = new HeartBeatCallback<Long>() {
//
//            @Override
//            public void onHeartBeat(HeartBeatPayLoad<Long> payLoad) {
//                log.info("Heartbeat {}", payLoad);
//            }
//        };
//
//        Collection<TradeableInstrument<String>> instruments = Arrays.asList(
//            new TradeableInstrument<>("XBTUSD"),
//            new TradeableInstrument<>("XBTJPY")
//        );
//        BitmexMarketDataStreamingService2 bitmexMarketDataStreamingService2 = new BitmexMarketDataStreamingService2(
//            marketEventCallback, instrumentEventCallback, heartBeatCallback, instruments);
//        bitmexMarketDataStreamingService2.init();
//        bitmexMarketDataStreamingService2.startMarketDataStreaming();

//        EventCallback<JSONObject> eventCallback = new EventCallback<JSONObject>() {
//            @Override
//            public void onEvent(EventPayLoad<JSONObject> eventPayLoad) {
//                log.info("Event: {}", eventPayLoad.getPayLoad().toString());
//            }
//        };

//        BitmexEventsStreamingService2 bitmexEventsStreamingService2 = new BitmexEventsStreamingService2(
//            eventCallback, heartBeatCallback);
//        bitmexEventsStreamingService2.init();
//        bitmexEventsStreamingService2.startEventsStreaming();
//
//        Uninterruptibles.sleepUninterruptibly(100_000L, TimeUnit.MILLISECONDS);
//
//        bitmexMarketDataStreamingService2.shutdown();
        //bitmexEventsStreamingService2.shutdown();

//        BitmexOrderManagementProvider bitmexOrderManagementProvider = new BitmexOrderManagementProvider();
//        Collection<Order<String, String>> orders = bitmexOrderManagementProvider.allPendingOrders();

        BitmexTradeManagementProvider bitmexTradeManagementProvider = new BitmexTradeManagementProvider();
        Collection<Trade<String, String, Long>> trades =
            bitmexTradeManagementProvider.getTradesForAccount(accounts.iterator().next().getAccountId());

        int t = 0;

    }

}
