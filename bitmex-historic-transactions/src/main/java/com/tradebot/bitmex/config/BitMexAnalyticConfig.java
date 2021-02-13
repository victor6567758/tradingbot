package com.tradebot.bitmex.config;

import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
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
}
