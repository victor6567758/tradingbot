package com.tradebot.core.instrument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class InstrumentService {

    private final Map<String, TradeableInstrument> instrumentMap;

    public InstrumentService(InstrumentDataProvider instrumentDataProvider) {
        Preconditions.checkNotNull(instrumentDataProvider);

        instrumentMap = ImmutableMap.<String, TradeableInstrument>builder().putAll(
            instrumentDataProvider.getInstruments().stream()
                .collect(Collectors.toMap(TradeableInstrument::getInstrument,
                    instrument -> instrument))).build();
    }

    public Collection<TradeableInstrument> getInstruments() {
        return this.instrumentMap.values();
    }

    public Collection<TradeableInstrument> getAllPairsWithCurrency(String currency) {
        if (StringUtils.isEmpty(currency)) {
            return Collections.emptyList();
        }

        return instrumentMap.entrySet().stream()
            .filter(entry -> entry.getKey().contains(currency))
            .map(Entry::getValue)
            .collect(Collectors.toList());
    }

    public Double getTickSizeForInstrument(TradeableInstrument instrument) {
        Preconditions.checkNotNull(instrument);

        TradeableInstrument tradeableInstrument = instrumentMap.get(instrument.getInstrument());
        return tradeableInstrument != null ? tradeableInstrument.getTickSize() : 1.0;
    }

    public TradeableInstrument resolveTradeableInstrument(String symbol) {
        TradeableInstrument tradeableInstrument = instrumentMap.get(symbol);
        if (tradeableInstrument == null) {
            throw new IllegalArgumentException(String.format("Unknown instrument %s", symbol));
        }

        return tradeableInstrument;
    }

    public TradeableInstrument resolveTradeableInstrumentNoException(String symbol) {
        TradeableInstrument tradeableInstrument = instrumentMap.get(symbol);
        if (tradeableInstrument == null) {
            return null;
        }

        return tradeableInstrument;
    }
}
