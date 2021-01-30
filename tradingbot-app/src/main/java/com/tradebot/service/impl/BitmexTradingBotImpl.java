package com.tradebot.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.Account;
import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.response.GridContextResponse;
import com.tradebot.service.BitmexTradingBot;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BitmexTradingBotImpl extends BitmexTradingBot {

    @Getter
    @RequiredArgsConstructor
    private static class InitialContext {

        private final double x;
        private final double priceEnd;
        private final int linesNum;

    }

    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    private static class TradingContext {

        private final TradeableInstrument tradeableInstrument;
        private final Map<BigDecimal, TradingDecision> tradingGrid = new TreeMap<>();
        private double oneLotPrice;
    }

    private Map<TradeableInstrument, InitialContext> initialContextMap;

    private Map<TradeableInstrument, TradingContext> tradingContextMap = new HashMap<>();
    private final Cache<DateTime, TradingContext> tradingContextCache;
    private final ReadWriteLock tradingContextMapLock = new ReentrantReadWriteLock();

    public BitmexTradingBotImpl(EventBus eventBus) {
        super(eventBus);
        tradingContextCache = CacheBuilder.newBuilder()
            .expireAfterWrite(bitmexAccountConfiguration.getBitmex().getTradingConfiguration().getTradingSolutionsDepthMin(),
                TimeUnit.MINUTES).build();
    }

    @PostConstruct
    public void initialize() {
        super.initialize();
        initializeDetails();
    }

    @PreDestroy
    public void deinitialize() {
        super.deinitialize();
    }

    public List<GridContextResponse> getLastContextList() {
        tradingContextMapLock.readLock().lock();
        try {

        } finally {
            tradingContextMapLock.readLock().unlock();
        }
    }

    public void onTradeSolution(CandleStick candleStick, CacheCandlestick cacheCandlestick) {
        if (candleStick.getCandleGranularity() == CandleStickGranularity.M1) {
            log.info("Trade solution detected on this candle {}", candleStick.toString());
        }

        Account<Long> account = accountDataProviderService.getLatestAccountInfo(bitmexAccountConfiguration.getBitmex()
            .getTradingConfiguration().getAccountId());

        InitialContext initialContext = initialContextMap.get(candleStick.getInstrument());
        if (initialContext == null) {
            throw new IllegalArgumentException(String.format("Cannot find symbol %s", candleStick.getInstrument()));
        }

        tradingContextMapLock.writeLock().lock();
        try {
            TradingContext tradingContext = new TradingContext(candleStick.getInstrument());

            tradingContext.getTradingGrid().clear();
            double currentPrice = candleStick.getClosePrice();
            double priceStep = (initialContext.getPriceEnd() - currentPrice) / initialContext.getLinesNum();

            for (int i = 0; i < initialContext.getLinesNum(); i++) {
                tradingContext.getTradingGrid().put(BigDecimal.valueOf(currentPrice), new TradingDecision(candleStick.getInstrument(), TradingSignal.LONG));
                currentPrice += priceStep;
            }

            tradingContext.setOneLotPrice(account.getTotalBalance().doubleValue() / initialContext.getLinesNum());
            tradingContextMap.put(candleStick.getInstrument(), tradingContext);
            tradingContextCache.put(candleStick.getEventDate(), tradingContext);

            if (log.isDebugEnabled()) {
                log.debug("Trading setup is done: {}", tradingContext.toString());
            }
        } finally {
            tradingContextMapLock.writeLock().unlock();
        }

    }

    @Override
    public void onTradeSolution(Price price, Cache<DateTime, Price> instrumentRecentPricesCache) {
        if (log.isDebugEnabled()) {
            log.debug("Trade solution detected on this market data {}", price.toString());
        }
    }


    private void initializeDetails() {

        initialContextMap = algParameters.entrySet()
            .stream()
            .map(
                entry -> new ImmutablePair<>(entry.getKey(),
                    new InitialContext(
                        (Double) entry.getValue().get("x"),
                        (Double) entry.getValue().get("priceEnd"),
                        (Integer) entry.getValue().get("linesNum")
                    ))
            ).collect(Collectors.toUnmodifiableMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight));
    }


}
