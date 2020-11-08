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
import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;


public class BitmexPositionManagementProviderTest {

    private static final TradeableInstrument<String> INSTRUMENT_XBTUSD = new TradeableInstrument<>("XBTUSD");
    private static final TradeableInstrument<String> INSTRUMENT_XBTJPY = new TradeableInstrument<>("XBTJPY");

    private final JSON json = new JSON();
    private final PositionApi positionApi = mock(PositionApi.class);
    private final OrderApi orderApi = mock(OrderApi.class);

    private List<Position> positions;
    private BitmexPositionManagementProvider bitmexPositionManagementProviderSpy;

    @Before
    public void init() throws ApiException, IOException {
        bitmexPositionManagementProviderSpy = spy(new BitmexPositionManagementProvider());

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
        Position storedPosition = positions.stream().filter(n -> n.getSymbol().equals(INSTRUMENT_XBTUSD.getInstrument())).findAny().orElseThrow();
        com.tradebot.core.position.Position<String> resolvedPosition =
            bitmexPositionManagementProviderSpy.getPositionForInstrument(storedPosition.getAccount().longValue(), INSTRUMENT_XBTUSD);
        assertThat(resolvedPosition.getUnits()).isEqualTo(storedPosition.getCurrentQty().longValue());
        assertThat(resolvedPosition.getInstrument().getInstrument()).isEqualTo(INSTRUMENT_XBTUSD.getInstrument());
        assertThat(resolvedPosition.getAveragePrice()).isCloseTo(storedPosition.getAvgCostPrice(), Offset.offset(0.0001));
        assertThat(resolvedPosition.getSide()).isEqualTo(TradingSignal.SHORT);
    }

    @Test
    public void testGetPositionsForAccount() {
        Position storedPositionXbtUsd =
            positions.stream().filter(n -> n.getSymbol().equals(INSTRUMENT_XBTUSD.getInstrument())).findAny().orElseThrow();
        Collection<com.tradebot.core.position.Position<String>> allPositions =
            bitmexPositionManagementProviderSpy.getPositionsForAccount(storedPositionXbtUsd.getAccount().longValue());

        assertThat(allPositions.stream().anyMatch(n -> n.getInstrument().getInstrument().equals(INSTRUMENT_XBTUSD.getInstrument()))).isTrue();
        assertThat(allPositions.stream().anyMatch(n -> n.getInstrument().getInstrument().equals(INSTRUMENT_XBTJPY.getInstrument()))).isTrue();
    }

    @Test
    public void testClosePosition() {
        Position storedPosition = positions.stream().filter(n -> n.getSymbol().equals(INSTRUMENT_XBTUSD.getInstrument())).findAny().orElseThrow();
        boolean result = bitmexPositionManagementProviderSpy.closePosition(storedPosition.getAccount().longValue(), INSTRUMENT_XBTUSD, 0);
        assertThat(result).isTrue();
    }

}