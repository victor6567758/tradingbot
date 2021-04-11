package com.tradebot.bitmex.config;

import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.account.transaction.TransactionInfoService;
import com.tradebot.core.instrument.InstrumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Slf4j
public class BitMexAnalyticConfig {

    private final TransactionDataProvider<String, Long> transactionDataProvider;

    private final AccountDataProvider<Long> accountDataProvider;

    public BitMexAnalyticConfig(
        @Lazy TransactionDataProvider<String, Long> transactionDataProvider,
        @Lazy AccountDataProvider<Long> accountDataProvider) {
        this.transactionDataProvider = transactionDataProvider;
        this.accountDataProvider = accountDataProvider;
    }

    @Bean
    public BitmexAccountConfiguration bitmexAccountConfiguration() {
        return BitmexUtils.readBitmexConfiguration();
    }

    @Bean
    public AccountDataProvider<Long> accountDataProvider() {
        return new BitmexAccountDataProviderService();
    }

    @Bean
    public TransactionDataProvider<String, Long> transactionDataProvider() {
        return new BitmexTransactionDataProviderService(new InstrumentService(new BitmexInstrumentDataProviderService(), operationResultContext -> {
            if (log.isDebugEnabled()) {
                log.debug(operationResultContext.toString());
            }
        }));
    }

    @Bean
    public TransactionInfoService<String, Long> transactionInfoService() {
        return new TransactionInfoService<>(transactionDataProvider, operationResultContext -> {
            if (log.isDebugEnabled()) {
                log.debug(operationResultContext.toString());
            }
        });
    }
}
