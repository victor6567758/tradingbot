package com.tradebot.config;

import com.google.common.eventbus.EventBus;
import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.bitmex.restapi.marketdata.historic.BitmexHistoricMarketDataProvider;
import com.tradebot.bitmex.restapi.order.BitmexOrderManagementProvider;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.marketdata.historic.HistoricMarketDataProvider;
import com.tradebot.core.order.OrderManagementProvider;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class TradingBotConfiguration {

    private final InstrumentService instrumentService;
    private final OrderManagementProvider<String, Long> orderManagementProvider;

    public TradingBotConfiguration(@Lazy InstrumentService instrumentService,
        @Lazy OrderManagementProvider<String, Long> orderManagementProvider) {
        this.instrumentService = instrumentService;
        this.orderManagementProvider = orderManagementProvider;
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
    public InstrumentService instrumentService() {
        return new InstrumentService(new BitmexInstrumentDataProviderService());
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
        return new BitmexOrderManagementProvider(instrumentService);
    }

}
