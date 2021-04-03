package com.tradebot.bitmex.restapi.instrument;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.InstrumentApi;
import com.tradebot.bitmex.restapi.generated.model.Instrument;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;


@Slf4j
public class BitmexInstrumentDataProviderService implements InstrumentDataProvider {

    private static final String INSTRUMENT_ERROR = "Instrument retrieving error {} {}";

    private static final BigDecimal CHUNK_SIZE = BigDecimal.valueOf(500);
    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexConfiguration();


    @Getter(AccessLevel.PACKAGE)
    private final InstrumentApi instrumentApi = new InstrumentApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public OperationResultContext<Collection<TradeableInstrument>> getInstruments() {

        try {
            List<TradeableInstrument> allInstruments = new ArrayList<>();
            BigDecimal position = BigDecimal.ZERO;

            ApiResponse<List<Instrument>> lastApiResponse;
            while (true) {
                lastApiResponse = getInstrumentApi().instrumentGetWithHttpInfo(
                    null,
                    null,
                    null,
                    CHUNK_SIZE,
                    position,
                    true,
                    null,
                    null
                );

                if (CollectionUtils.isEmpty(lastApiResponse.getData())) {
                    break;
                }
                allInstruments.addAll(lastApiResponse.getData().stream().map(BitmexInstrumentDataProviderService::toTradeableInstrument)
                    .collect(Collectors.toList()));
                position = position.add(CHUNK_SIZE);
            }

            return BitmexUtils.prepareResultReturned(lastApiResponse, new BitmexOperationQuotas<>(allInstruments));
        } catch (ApiException apiException) {
            log.error(INSTRUMENT_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }

    }

    private static TradeableInstrument toTradeableInstrument(Instrument instrument) {
        return new TradeableInstrument(
            instrument.getSymbol(),
            instrument.getSymbol(),
            instrument.getTickSize(),
            null,
            instrument.getSymbol(),
            instrument.getLotSize(),
            instrument.getMultiplier(),
            instrument.getPositionCurrency()
        );
    }
}
