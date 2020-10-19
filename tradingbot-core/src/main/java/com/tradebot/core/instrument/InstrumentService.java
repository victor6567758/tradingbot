package com.tradebot.core.instrument;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class InstrumentService<T> {

    private final Map<String, TradeableInstrument<T>> instrumentMap;

    public InstrumentService(InstrumentDataProvider<T> instrumentDataProvider) {
        Preconditions.checkNotNull(instrumentDataProvider);

        instrumentMap = ImmutableMap.<String, TradeableInstrument<T>>builder().putAll(
            instrumentDataProvider.getInstruments().stream()
                .collect(Collectors.toMap(TradeableInstrument::getInstrument,
                    instrument -> instrument))).build();
    }

    public Collection<TradeableInstrument<T>> getInstruments() {
        return this.instrumentMap.values();
    }

    public Collection<TradeableInstrument<T>> getAllPairsWithCurrency(String currency) {
        if (StringUtils.isEmpty(currency)) {
            return Collections.emptyList();
        }

        return instrumentMap.entrySet().stream()
            .filter(entry -> entry.getKey().contains(currency))
            .map(Entry::getValue)
            .collect(Collectors.toList());
    }

    public Double getPipForInstrument(TradeableInstrument<T> instrument) {
        Preconditions.checkNotNull(instrument);

        TradeableInstrument<T> tradeableInstrument = instrumentMap.get(instrument.getInstrument());
        return tradeableInstrument != null ? tradeableInstrument.getPip() : 1.0;

    }
}
