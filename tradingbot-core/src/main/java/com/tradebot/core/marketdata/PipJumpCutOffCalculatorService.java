package com.tradebot.core.marketdata;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;

public class PipJumpCutOffCalculatorService<T> implements PipJumpCutOffCalculator<T> {

    // TODO: why locks as this Cache is thread safe ?
    private final Cache<TradeableInstrument, Double> offsetCache;
    private final TradeableInstrument refInstrument;
    private final CurrentPriceInfoProvider currentPriceInfoProvider;
    private final Double refInstrumentPip;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final InstrumentService instrumentService;

    public PipJumpCutOffCalculatorService(TradeableInstrument refInstrument,
            CurrentPriceInfoProvider currentPriceInfoProvider, Double refInstrumentPip,
            InstrumentService instrumentService) {
        this.refInstrument = refInstrument;
        this.currentPriceInfoProvider = currentPriceInfoProvider;
        this.refInstrumentPip = refInstrumentPip;
        this.instrumentService = instrumentService;
        offsetCache = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build();
    }

    @SuppressWarnings("unchecked")
    private Double fetchSingleInstrumentPrice(TradeableInstrument instrument) {
        Map<TradeableInstrument, Price> priceMap = currentPriceInfoProvider
                .getCurrentPricesForInstruments(Lists.newArrayList(instrument));
        Price price = priceMap.get(instrument);
        Double instrumentPrice = (price.getAskPrice() + price.getBidPrice()) / 2;
        Lock writeLock = this.lock.writeLock();
        try {
            writeLock.lock();
            this.offsetCache.put(instrument, instrumentPrice);
        } finally {
            writeLock.unlock();
        }
        return instrumentPrice;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double calculatePipJumpCutOff(TradeableInstrument instrument) {
        Double refInstrumentPrice = this.offsetCache.getIfPresent(refInstrument);
        Double instrumentPrice = this.offsetCache.getIfPresent(instrument);
        if (refInstrumentPrice == null && instrumentPrice == null) {
            Map<TradeableInstrument, Price> priceMap = this.currentPriceInfoProvider
                    .getCurrentPricesForInstruments(Lists.newArrayList(refInstrument, instrument));

            Price refPrice = priceMap.get(refInstrument);
            refInstrumentPrice = (refPrice.getAskPrice() + refPrice.getBidPrice()) / 2;
            Price price = priceMap.get(instrument);
            instrumentPrice = (price.getAskPrice() + price.getBidPrice()) / 2;
            Lock writeLock = this.lock.writeLock();
            try {
                writeLock.lock();
                this.offsetCache.put(refInstrument, refInstrumentPrice);
                this.offsetCache.put(instrument, instrumentPrice);
            } finally {
                writeLock.unlock();
            }
        } else if (refInstrumentPrice == null) {
            refInstrumentPrice = fetchSingleInstrumentPrice(refInstrument);
        } else if (instrumentPrice == null) {
            instrumentPrice = fetchSingleInstrumentPrice(instrument);
        }
        double pipRef = this.instrumentService.getTickSizeForInstrument(refInstrument);
        double pip = this.instrumentService.getTickSizeForInstrument(instrument);
        return ((instrumentPrice * pipRef) / (refInstrumentPrice * pip)) * refInstrumentPip;
    }

}
