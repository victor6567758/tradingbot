package com.tradebot.core.instrument;

import com.google.common.collect.ImmutableMap;
import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class InstrumentService {

    private static final String INVALID_INSTRUMENT_PROVIDER_RESULT_S = "Invalid instrument provider result: %s";

    private final Map<String, TradeableInstrument> instrumentMap;
    private final OperationResultCallback operationResultCallback;

    public InstrumentService(
        InstrumentDataProvider instrumentDataProvider,
        OperationResultCallback operationResultCallback) {

        this.operationResultCallback = operationResultCallback;

        OperationResultContext<Collection<TradeableInstrument>> result = instrumentDataProvider.getInstruments();
        operationResultCallback.onOperationResult(result);

        if (result.isResult()) {
            instrumentMap = ImmutableMap.<String, TradeableInstrument>builder().putAll(
                result.getData().stream()
                    .collect(Collectors.toMap(TradeableInstrument::getInstrument,
                        instrument -> instrument))).build();
        } else {
            throw new IllegalArgumentException(String.format(INVALID_INSTRUMENT_PROVIDER_RESULT_S, result.getMessage()));
        }
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
