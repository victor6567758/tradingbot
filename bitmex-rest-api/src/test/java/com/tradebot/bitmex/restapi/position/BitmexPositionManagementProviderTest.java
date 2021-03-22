package com.tradebot.bitmex.restapi.position;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.TradingSignal;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpStatus;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;


public class BitmexPositionManagementProviderTest {

    private static final TradeableInstrument XBTUSD_INSTR =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);
    private static final TradeableInstrument XBTJPY_INSTR =
        new TradeableInstrument("XBTJPY", "XBTJPY", 100, null, null, BigDecimal.valueOf(1L), null, null);

    private static final String ORDER_ID = "12345";

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

        ApiResponse<List<Order>> positionsResponse = new ApiResponse(HttpStatus.SC_OK, Collections.emptyMap(), positions);
        doReturn(positionsResponse).when(positionApi).positionGetWithHttpInfo(
            isNull(),
            isNull(),
            isNull());

        doReturn(positionApi).when(bitmexPositionManagementProviderSpy).getPositionApi();

        Order order = new Order();
        order.setOrderID(ORDER_ID);

        ApiResponse<Order> ordersResponse = new ApiResponse(HttpStatus.SC_OK, Collections.emptyMap(), order);
        doReturn(ordersResponse).when(orderApi).orderClosePositionWithHttpInfo(
            anyString(),
            isNull());

        doReturn(orderApi).when(bitmexPositionManagementProviderSpy).getOrderApi();
    }

    @Test
    public void testGetPositionForInstrument() {
        Position storedPosition = positions.stream().filter(n -> n.getSymbol().equals(XBTUSD_INSTR.getInstrument())).findAny().orElseThrow();
        OperationResultContext<com.tradebot.core.position.Position> resolvedPosition =
            bitmexPositionManagementProviderSpy.getPositionForInstrument(storedPosition.getAccount().longValue(), XBTUSD_INSTR);

        assertThat(resolvedPosition.getData().getUnits()).isEqualTo(storedPosition.getCurrentQty().longValue());
        assertThat(resolvedPosition.getData().getInstrument().getInstrument()).isEqualTo(XBTUSD_INSTR.getInstrument());
        assertThat(resolvedPosition.getData().getAveragePrice()).isCloseTo(storedPosition.getAvgCostPrice(), Offset.offset(0.0001));
        assertThat(resolvedPosition.getData().getSide()).isEqualTo(TradingSignal.SHORT);
    }

    @Test
    public void testGetPositionsForAccount() {
        Position storedPositionXbtUsd =
            positions.stream().filter(n -> n.getSymbol().equals(XBTUSD_INSTR.getInstrument())).findAny().orElseThrow();
        OperationResultContext<Collection<com.tradebot.core.position.Position>> allPositions =
            bitmexPositionManagementProviderSpy.getPositionsForAccount(storedPositionXbtUsd.getAccount().longValue());

        assertThat(allPositions.getData().stream().anyMatch(n -> n.getInstrument().getInstrument().equals(XBTUSD_INSTR.getInstrument()))).isTrue();
        assertThat(allPositions.getData().stream().anyMatch(n -> n.getInstrument().getInstrument().equals(XBTJPY_INSTR.getInstrument()))).isTrue();
    }

    @Test
    public void testClosePosition() {
        Position storedPosition = positions.stream().filter(n -> n.getSymbol().equals(XBTUSD_INSTR.getInstrument())).findAny().orElseThrow();
        OperationResultContext<String> result =
            bitmexPositionManagementProviderSpy.closePosition(storedPosition.getAccount().longValue(), XBTUSD_INSTR, 0);
        assertThat(result.getData()).isEqualTo(ORDER_ID);
    }

}