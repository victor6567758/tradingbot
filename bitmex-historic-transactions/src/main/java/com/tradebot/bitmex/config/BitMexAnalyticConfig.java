package com.tradebot.bitmex.config;

import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BitMexAnalyticConfig {

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
}
