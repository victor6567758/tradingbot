package com.tradebot.app;


import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.position.BitmexPositionManagementProvider;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.position.Position;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
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
//
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

        BitmexPositionManagementProvider bitmexPositionManagementProvider = new BitmexPositionManagementProvider();
        Position<String> posXbtUsd = bitmexPositionManagementProvider.getPositionForInstrument(accounts.iterator().next().getAccountId(),
            new TradeableInstrument<>("XBTUSD"));

        Collection<Position<String>> allPositions =
            bitmexPositionManagementProvider.getPositionsForAccount(accounts.iterator().next().getAccountId());

        int t = 0;

    }

}
