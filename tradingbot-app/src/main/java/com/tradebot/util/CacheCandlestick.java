package com.tradebot.util;

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
import org.joda.time.DateTime;
import org.springframework.util.CollectionUtils;


@Slf4j
public class CacheCandlestick {

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

//    public void addTick(MarketDataPayLoad marketDataPayLoad) {
//        for(Map.Entry<CandleStickGranularity, Cache<DateTime, CandleStick>> entry: cache.entrySet()) {
//
//        }
//    }

    public Map<DateTime, CandleStick> getValuesForGranularity(CandleStickGranularity candleStickGranularity) {
        Cache<DateTime, CandleStick> cacheResolved = cache.get(candleStickGranularity);
        if (cacheResolved == null) {
            throw new IllegalArgumentException(String.format("This candlestick %s is not of correct granularity",
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
            throw new IllegalArgumentException(String.format("This candlestick %s is not of correct instrument %s",
                candleStickFirst.toString(), instrument));
        }

        Cache<DateTime, CandleStick> cacheResolved = cache.get(candleStickFirst.getCandleGranularity());
        if (cacheResolved == null) {
            throw new IllegalArgumentException(String.format("This candlestick %s is not of correct granularity",
                candleStickFirst.toString()));
        }

        for (CandleStick candleStick : newCandleSticks) {

            if (candleStickFirst.getCandleGranularity() != candleStick.getCandleGranularity()) {
                throw new IllegalArgumentException(String.format("This candlestick %s is not of the same granularity as %s",
                    candleStickFirst.toString(), candleStick.toString()));
            }

            cacheResolved.put(candleStick.getEventDate(), candleStick);
        }
    }


}
