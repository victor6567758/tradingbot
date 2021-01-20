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

    public Double getPipForInstrument(TradeableInstrument instrument) {
        Preconditions.checkNotNull(instrument);

        TradeableInstrument tradeableInstrument = instrumentMap.get(instrument.getInstrument());
        return tradeableInstrument != null ? tradeableInstrument.getPip() : 1.0;

    }
}
