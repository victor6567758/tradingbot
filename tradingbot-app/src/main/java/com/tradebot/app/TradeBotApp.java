package com.tradebot.app;


import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.Transaction;
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

        AccountDataProvider accountDataProvider = new BitmexAccountDataProviderService();
        Collection<Account> accounts = accountDataProvider.getLatestAccountsInfo();

        BitmexTransactionDataProviderService bitmexTransactionDataProviderService = new BitmexTransactionDataProviderService();
        Transaction transaction = bitmexTransactionDataProviderService
            .getTransaction("00000000-0000-0000-0000-000000000000", (Long) accounts.iterator().next().getAccountId());

        int t = 0;

    }

}
