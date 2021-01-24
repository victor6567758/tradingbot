package com.tradebot.core.helper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;

@Slf4j
public class CacheCandlestick {

    private static final String NOT_OF_CORRECT_GRANULARITY = "This candlestick %s is not of correct granularity";
    private static final String NOT_OF_CORRECT_INSTRUMENT = "This candlestick %s is not of correct instrument %s";
    private static final String NOT_OF_THE_SAME_GRANULARITY_AS = "This candlestick %s is not of the same granularity as %s";

    private final TradeableInstrument instrument;
    private final Map<CandleStickGranularity, Cache<DateTime, CandleStick>> cache = new EnumMap<>(CandleStickGranularity.class);

    public CacheCandlestick(TradeableInstrument instrument, int lengthInMinutes, List<CandleStickGranularity> granularities) {
        if (lengthInMinutes <= 0) {
            throw new IllegalArgumentException("Invalid cache size");
        }

        for (CandleStickGranularity granularity : granularities) {
            cache.put(granularity, CacheBuilder.newBuilder()
                .expireAfterWrite(lengthInMinutes,
                    TimeUnit.MINUTES).build());
        }

        this.instrument = instrument;
    }

    public void addCandlestick(CandleStick candleStick) {
        Cache<DateTime, CandleStick> cacheResolved = cache.get(candleStick.getCandleGranularity());
        if (cacheResolved == null) {
            throw new IllegalArgumentException(String.format(NOT_OF_CORRECT_GRANULARITY,
                candleStick.getCandleGranularity()));
        }

        if (!candleStick.getInstrument().equals(instrument)) {
            throw new IllegalArgumentException(String.format(NOT_OF_CORRECT_INSTRUMENT,
                candleStick.toString(), instrument));
        }

        cacheResolved.put(candleStick.getEventDate(), candleStick);

        if (log.isTraceEnabled()) {
            log.trace("Candlestick was added to cache {}", candleStick.toString());
        }
    }

    public Map<DateTime, CandleStick> getValuesForGranularity(CandleStickGranularity candleStickGranularity) {
        Cache<DateTime, CandleStick> cacheResolved = cache.get(candleStickGranularity);
        if (cacheResolved == null) {
            throw new IllegalArgumentException(String.format(NOT_OF_CORRECT_GRANULARITY,
                candleStickGranularity));
        }

        return cacheResolved.asMap();
    }

    public void addHistory(TradeableInstrument instrument, List<CandleStick> newCandleSticks) {

        if (CollectionUtils.isEmpty(newCandleSticks)) {
            return;
        }

        CandleStick candleStickFirst = newCandleSticks.get(0);

        if (!candleStickFirst.getInstrument().equals(instrument)) {
            throw new IllegalArgumentException(String.format(NOT_OF_CORRECT_INSTRUMENT,
                candleStickFirst.toString(), instrument));
        }

        Cache<DateTime, CandleStick> cacheResolved = cache.get(candleStickFirst.getCandleGranularity());
        if (cacheResolved == null) {
            throw new IllegalArgumentException(String.format(NOT_OF_CORRECT_GRANULARITY,
                candleStickFirst.toString()));
        }

        for (CandleStick candleStick : newCandleSticks) {

            if (candleStickFirst.getCandleGranularity() != candleStick.getCandleGranularity()) {
                throw new IllegalArgumentException(String.format(NOT_OF_THE_SAME_GRANULARITY_AS,
                    candleStickFirst.toString(), candleStick.toString()));
            }

            cacheResolved.put(candleStick.getEventDate(), candleStick);

            if (log.isTraceEnabled()) {
                log.trace("Historical candlestick was added to cache {}", candleStick.toString());
            }
        }
    }
}
