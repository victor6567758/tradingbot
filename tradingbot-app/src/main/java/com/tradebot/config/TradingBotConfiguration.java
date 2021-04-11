package com.tradebot.config;

import com.google.common.eventbus.EventBus;
import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.marketdata.historic.BitmexHistoricMarketDataProvider;
import com.tradebot.bitmex.restapi.order.BitmexOrderManagementProvider;
import com.tradebot.bitmex.restapi.position.BitmexPositionManagementProvider;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.marketdata.historic.HistoricMarketDataProvider;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.position.PositionManagementProvider;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Slf4j
public class TradingBotConfiguration {

    private final InstrumentDataProvider instrumentDataProvider;

    public TradingBotConfiguration(@Lazy InstrumentDataProvider instrumentDataProvider) {
        this.instrumentDataProvider = instrumentDataProvider;
    }

    @Bean
    public BitmexAccountConfiguration bitmexAccountConfiguration() {
        return BitmexUtils.readBitmexConfiguration();
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public InstrumentDataProvider instrumentDataProvider() {
        return new BitmexInstrumentDataProviderService();
    }

    @Bean
    public AccountDataProvider<Long> accountDataProvider() {
        return new BitmexAccountDataProviderService();
    }

    @Bean
    public HistoricMarketDataProvider historicMarketDataProvider() {
        return new BitmexHistoricMarketDataProvider();
    }

    @Bean
    public OrderManagementProvider<String, Long> orderManagementProvider() {
        return new BitmexOrderManagementProvider(new InstrumentService(instrumentDataProvider, operationResultContext -> {
            if (log.isDebugEnabled()) {
                log.debug("Instrument operation result (order quotas) callback {}", operationResultContext.toString());
            }
        }));
    }

    @Bean
    public PositionManagementProvider<Long> positionManagementProvider() {
        return new BitmexPositionManagementProvider(new InstrumentService(instrumentDataProvider, operationResultContext -> {
            if (log.isDebugEnabled()) {
                log.debug("Instrument operation result (order quotas) callback {}", operationResultContext.toString());
            }
        }));
    }

}
