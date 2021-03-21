package com.tradebot.bitmex.restapi.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;


public class BitmexPositionManagementProviderTest {

    private static final TradeableInstrument XBTUSD_INSTR =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);
    private static final TradeableInstrument XBTJPY_INSTR =
        new TradeableInstrument("XBTJPY", "XBTJPY", 100, null, null, BigDecimal.valueOf(1L), null, null);

    private final JSON json = new JSON();
    private final PositionApi positionApi = mock(PositionApi.class);
    private final OrderApi orderApi = mock(OrderApi.class);

    private List<Position> positions;
    private BitmexPositionManagementProvider bitmexPositionManagementProviderSpy;
    private InstrumentService instrumentServiceSpy;

    @Before
    public void init() throws ApiException, IOException {
        instrumentServiceSpy = mock(InstrumentService.class);
        doReturn(XBTUSD_INSTR).when(instrumentServiceSpy).resolveTradeableInstrument(XBTUSD_INSTR.getInstrument());
        doReturn(XBTJPY_INSTR).when(instrumentServiceSpy).resolveTradeableInstrument(XBTJPY_INSTR.getInstrument());

        bitmexPositionManagementProviderSpy = spy(new BitmexPositionManagementProvider(instrumentServiceSpy));

        positions = json.deserialize(Resources.toString(Resources.getResource("positionsAll.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Position>>() {
            }.getType());

        when(positionApi.positionGet(
            isNull(),
            isNull(),
            isNull())
        ).thenReturn(positions);

        doReturn(positionApi).when(bitmexPositionManagementProviderSpy).getPositionApi();
        doReturn(orderApi).when(bitmexPositionManagementProviderSpy).getOrderApi();
    }

    @Test
    public void testGetPositionForInstrument() {
        Position storedPosition = positions.stream().filter(n -> n.getSymbol().equals(XBTUSD_INSTR.getInstrument())).findAny().orElseThrow();
        com.tradebot.core.position.Position resolvedPosition =
            bitmexPositionManagementProviderSpy.getPositionForInstrument(storedPosition.getAccount().longValue(), XBTUSD_INSTR);
        assertThat(resolvedPosition.getUnits()).isEqualTo(storedPosition.getCurrentQty().longValue());
        assertThat(resolvedPosition.getInstrument().getInstrument()).isEqualTo(XBTUSD_INSTR.getInstrument());
        assertThat(resolvedPosition.getAveragePrice()).isCloseTo(storedPosition.getAvgCostPrice(), Offset.offset(0.0001));
        assertThat(resolvedPosition.getSide()).isEqualTo(TradingSignal.SHORT);
    }

    @Test
    public void testGetPositionsForAccount() {
        Position storedPositionXbtUsd =
            positions.stream().filter(n -> n.getSymbol().equals(XBTUSD_INSTR.getInstrument())).findAny().orElseThrow();
        Collection<com.tradebot.core.position.Position> allPositions =
            bitmexPositionManagementProviderSpy.getPositionsForAccount(storedPositionXbtUsd.getAccount().longValue());

        assertThat(allPositions.stream().anyMatch(n -> n.getInstrument().getInstrument().equals(XBTUSD_INSTR.getInstrument()))).isTrue();
        assertThat(allPositions.stream().anyMatch(n -> n.getInstrument().getInstrument().equals(XBTJPY_INSTR.getInstrument()))).isTrue();
    }

    @Test
    public void testClosePosition() {
        Position storedPosition = positions.stream().filter(n -> n.getSymbol().equals(XBTUSD_INSTR.getInstrument())).findAny().orElseThrow();
        boolean result = bitmexPositionManagementProviderSpy.closePosition(storedPosition.getAccount().longValue(), XBTUSD_INSTR, 0);
        assertThat(result).isTrue();
    }

}