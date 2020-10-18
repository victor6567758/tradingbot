package com.precioustech.fxtrading.instrument;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

public class InstrumentService<T> {

    private final Map<String, TradeableInstrument<T>> instrumentMap;

    public InstrumentService(InstrumentDataProvider<T> instrumentDataProvider) {
        Preconditions.checkNotNull(instrumentDataProvider);
        Collection<TradeableInstrument<T>> instruments = instrumentDataProvider.getInstruments();
        Map<String, TradeableInstrument<T>> tradeableInstrumentMap = new TreeMap<>();
        for (TradeableInstrument<T> instrument : instruments) {
            tradeableInstrumentMap.put(instrument.getInstrument(), instrument);
        }
        instrumentMap = Collections.unmodifiableMap(tradeableInstrumentMap);
    }

    public Collection<TradeableInstrument<T>> getInstruments() {
        return this.instrumentMap.values();
    }

    public Collection<TradeableInstrument<T>> getAllPairsWithCurrency(String currency) {
        Collection<TradeableInstrument<T>> allPairs = new ArrayList<>(instrumentMap.size());
        if (StringUtils.isEmpty(currency)) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, TradeableInstrument<T>> entry : instrumentMap.entrySet()) {
            if (entry.getKey().contains(currency)) {
                allPairs.add(entry.getValue());
            }
        }
        return allPairs;
    }

    public Double getPipForInstrument(TradeableInstrument<T> instrument) {
        Preconditions.checkNotNull(instrument);
        TradeableInstrument<T> tradeableInstrument = instrumentMap.get(instrument.getInstrument());

        if (tradeableInstrument != null) {
            return tradeableInstrument.getPip();
        } else {
            return 1.0;
        }
    }
}
