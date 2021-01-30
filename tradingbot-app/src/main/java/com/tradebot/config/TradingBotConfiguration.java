package com.tradebot.config;

import com.google.common.eventbus.EventBus;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingBotConfiguration {

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
