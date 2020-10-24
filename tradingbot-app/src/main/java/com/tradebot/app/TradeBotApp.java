package com.tradebot.app;


import com.tradebot.bitmex.restapi.marketdata.historic.BitmexHistoricMarketDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import java.util.List;
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

//        AccountDataProvider accountDataProvider = new BitmexAccountDataProviderService();
//        Collection<Account> accounts = accountDataProvider.getLatestAccountsInfo();
//
//        BitmexTransactionDataProviderService bitmexTransactionDataProviderService = new BitmexTransactionDataProviderService();
//        Transaction transaction = bitmexTransactionDataProviderService
//            .getTransaction("00000000-0000-0000-0000-000000000000", (Long) accounts.iterator().next().getAccountId());
//
//        BitmexHistoricMarketDataProvider bitmexHistoricMarketDataProvider = new BitmexHistoricMarketDataProvider();
//        List<CandleStick<String>> data = bitmexHistoricMarketDataProvider.getCandleSticks(new TradeableInstrument<>("XBTUSD"),
//            CandleStickGranularity.M1, 100);

        int t = 0;

    }

}
