package com.tradebot.bitmex.restapi.instrument;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.InstrumentApi;
import com.tradebot.bitmex.restapi.generated.model.Instrument;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;


@Slf4j
public class BitmexInstrumentDataProviderService implements InstrumentDataProvider {

    private static final BigDecimal CHUNK_SIZE = BigDecimal.valueOf(500);
    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();


    @Getter(AccessLevel.PACKAGE)
    private final InstrumentApi instrumentApi = new InstrumentApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    @SneakyThrows
    public Collection<TradeableInstrument> getInstruments() {
        List<Instrument> instruments = getAllInstruments();
        return instruments.stream().map(BitmexInstrumentDataProviderService::toTradeableInstrument).collect(Collectors.toList());
    }

    private List<Instrument> getAllInstruments() throws ApiException {
        List<Instrument> allInstruments = new ArrayList<>();
        BigDecimal position = BigDecimal.ZERO;
        while (true) {
            List<Instrument> chunk = getInstrumentApi().instrumentGet(
                null,
                null,
                null,
                CHUNK_SIZE,
                position,
                true,
                null,
                null
            );

            if (CollectionUtils.isEmpty(chunk)) {
                break;
            }
            allInstruments.addAll(chunk);
            position = position.add(CHUNK_SIZE);
        }

        return allInstruments;
    }

    private static TradeableInstrument toTradeableInstrument(Instrument instrument) {
        return new TradeableInstrument(
            instrument.getSymbol(),
            instrument.getSymbol(),
            instrument.getTickSize(),
            null,
            instrument.getSymbol()
        );
    }
}
