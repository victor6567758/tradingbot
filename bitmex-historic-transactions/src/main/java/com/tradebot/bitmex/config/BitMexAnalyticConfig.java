package com.tradebot.bitmex.config;

import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class BitMexAnalyticConfig {

    private final InstrumentService instrumentService;

    public BitMexAnalyticConfig(@Lazy InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Bean
    public InstrumentService instrumentService() {
        return new InstrumentService(new BitmexInstrumentDataProviderService());
    }

    @Bean
    public TransactionDataProvider<String, Long> transactionDataProvider() {
        return new BitmexTransactionDataProviderService(instrumentService);
    }

    @Bean
    public BitmexAccountConfiguration bitmexAccountConfiguration() {
        return BitmexUtils.readBitmexConfiguration();
    }

    @Bean
    public AccountDataProvider<Long> accountDataProvider() {
        return new BitmexAccountDataProviderService();
    }
}
