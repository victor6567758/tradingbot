package com.tradebot.bitmex.restapi.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.TradeApi;
import com.tradebot.bitmex.restapi.generated.model.Trade;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class BitmexTradeManagementProviderTest {

    private static final long DUMMY_ACCOUNT = 12345L;
    private final JSON json = new JSON();
    private final TradeApi tradeApi = mock(TradeApi.class);

    private List<Trade> trades;
    private BitmexTradeManagementProvider bitmexTradeManagementProviderSpy;
    private InstrumentService instrumentServiceSpy;

    @Before
    public void init() throws IOException, ApiException {

        trades = json.deserialize(Resources.toString(Resources.getResource("tradesAll.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Trade>>() {
            }.getType());

        instrumentServiceSpy = mock(InstrumentService.class);
        trades.forEach(trade -> {
            TradeableInstrument instrument =
                new TradeableInstrument(trade.getSymbol(), trade.getSymbol(), 0.5, null, null, BigDecimal.valueOf(1L), null, null);
            doReturn(instrument).when(instrumentServiceSpy).resolveTradeableInstrument(instrument.getInstrument());
        });
        bitmexTradeManagementProviderSpy = spy(new BitmexTradeManagementProvider(instrumentServiceSpy));

        when(tradeApi.tradeGet(
            isNull(),
            isNull(),
            isNull(),
            any(BigDecimal.class),
            eq(BigDecimal.ZERO),
            eq(true),
            isNull(),
            isNull())
        ).thenReturn(trades);

        doReturn(tradeApi).when(bitmexTradeManagementProviderSpy).getTradeApi();

    }

    @Test
    public void testGetTradesForAccount() {
        Collection<com.tradebot.core.trade.Trade<String, Long>> trades =
            bitmexTradeManagementProviderSpy.getTradesForAccount(DUMMY_ACCOUNT);
        assertThat(trades.size()).isGreaterThan(1);
    }


}
