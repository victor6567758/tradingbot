package com.tradebot.service.impl;

import com.google.common.cache.Cache;
import com.google.common.eventbus.EventBus;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.marketdata.MarketDataPayLoad;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.service.BitmexTradingBot;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BitmexTradingBotImpl extends BitmexTradingBot {

    public BitmexTradingBotImpl(EventBus eventBus) {
        super(eventBus);
    }

    @PostConstruct
    public void initialize() {
        super.initialize();
    }

    @PreDestroy
    public void deinitialize() {
        super.deinitialize();
    }

    public void onTradeSolution(CandleStick candleStick, CacheCandlestick cacheCandlestick) {
        if (candleStick.getCandleGranularity() == CandleStickGranularity.M1) {
            log.info("Trade solution detected on this candle {}", candleStick.toString());
        }

    }

    @Override
    public void onTradeSolution(MarketDataPayLoad marketDataPayLoad, Cache<DateTime, MarketDataPayLoad> instrumentRecentPricesCache) {
        if (log.isDebugEnabled()) {
            log.debug("Trade solution detected on this market data {}", marketDataPayLoad.toString());
        }
    }
}
