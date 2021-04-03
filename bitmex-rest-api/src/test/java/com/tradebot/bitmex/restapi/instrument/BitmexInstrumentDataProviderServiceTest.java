package com.tradebot.bitmex.restapi.instrument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.InstrumentApi;
import com.tradebot.bitmex.restapi.generated.model.Instrument;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;


public class BitmexInstrumentDataProviderServiceTest {

    private static final BigDecimal CHUNK_SIZE = BigDecimal.valueOf(500);

    private final JSON json = new JSON();

    private BitmexInstrumentDataProviderService bitmexInstrumentDataProviderServiceSpy;
    private final InstrumentApi instrumentApi = mock(InstrumentApi.class);
    private List<Instrument> instruments;

    @Before
    public void init() throws IOException, ApiException {
        bitmexInstrumentDataProviderServiceSpy = spy(new BitmexInstrumentDataProviderService());

        instruments = json.deserialize(Resources.toString(Resources.getResource("instrumentsAll.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Instrument>>() {
            }.getType());

        when(instrumentApi.instrumentGetWithHttpInfo(
            isNull(),
            isNull(),
            isNull(),
            eq(CHUNK_SIZE),
            eq(BigDecimal.ZERO),
            eq(true),
            isNull(),
            isNull())
        ).thenReturn(new ApiResponse(HttpStatus.SC_OK, Collections.emptyMap(), instruments));

        when(instrumentApi.instrumentGetWithHttpInfo(
            isNull(),
            isNull(),
            isNull(),
            eq(CHUNK_SIZE),
            not(eq(BigDecimal.ZERO)),
            eq(true),
            isNull(),
            isNull())
        ).thenReturn(new ApiResponse(HttpStatus.SC_OK, Collections.emptyMap(), Collections.emptyList()));

        doReturn(instrumentApi).when(bitmexInstrumentDataProviderServiceSpy).getInstrumentApi();
    }

    @Test
    public void testGetInstruments() {
        OperationResultContext<Collection<TradeableInstrument>> tradebleInstruments = bitmexInstrumentDataProviderServiceSpy.getInstruments();
        assertThat(tradebleInstruments.getData()).hasSize(instruments.size());
        assertThat(tradebleInstruments.getData().stream().map(TradeableInstrument::getInstrument).distinct().collect(Collectors.toList()))
            .containsExactlyInAnyOrder(instruments.stream().map(Instrument::getSymbol).toArray(String[]::new));

    }

}
