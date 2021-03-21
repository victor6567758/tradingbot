package com.tradebot.bitmex.restapi.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.order.OrderStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BitmexOrderManagementProviderTest {

    private static final TradeableInstrument XBTUSD_INSTR =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);
    private static final TradeableInstrument XBTJPY_INSTR =
        new TradeableInstrument("XBTJPY", "XBTJPY", 100, null, null, BigDecimal.valueOf(1L), null, null);


    private final JSON json = new JSON();
    private final OrderApi orderApi = mock(OrderApi.class);

    private List<Order> orders;
    private Order newOrder;
    private BitmexOrderManagementProvider bitmexOrderManagementProviderSpy;
    private InstrumentService instrumentServiceSpy;



    @Before
    public void init() throws IOException, ApiException {
        instrumentServiceSpy = mock(InstrumentService.class);
        doReturn(XBTUSD_INSTR).when(instrumentServiceSpy).resolveTradeableInstrument(XBTUSD_INSTR.getInstrument());
        doReturn(XBTJPY_INSTR).when(instrumentServiceSpy).resolveTradeableInstrument(XBTJPY_INSTR.getInstrument());


        bitmexOrderManagementProviderSpy = spy(new BitmexOrderManagementProvider(instrumentServiceSpy));
        orders = json.deserialize(Resources.toString(Resources.getResource("ordersAll.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Order>>() {
            }.getType());
        newOrder = orders.stream().filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText())).findAny().orElseThrow();

        ApiResponse<List<Order>> ordersResponse = new ApiResponse(HttpStatus.SC_OK, Collections.emptyMap(), orders);
        doReturn(ordersResponse).when(orderApi).orderGetOrdersWithHttpInfo(
            isNull(),
            isNull(),
            isNull(),
            any(BigDecimal.class),
            eq(BigDecimal.ZERO),
            eq(true),
            isNull(),
            isNull()
        );

        doReturn(orderApi).when(bitmexOrderManagementProviderSpy).getOrderApi();

    }

    @Test
    public void testAllPendingOrders() {
        OperationResultContext<Collection<com.tradebot.core.order.Order<String>>> pendingOrders = bitmexOrderManagementProviderSpy.allPendingOrders();
        assertThat(pendingOrders.getData()).hasSize(1);

        com.tradebot.core.order.Order<String> pendingOrder = pendingOrders.getData().iterator().next();
        assertThat(pendingOrder.getOrderId()).isEqualTo(newOrder.getOrderID());
        assertThat(pendingOrder.getInstrument().getInstrument()).isEqualTo(newOrder.getSymbol());
        assertThat(pendingOrder.getSide()).isEqualTo(TradingSignalConvertible.fromString(newOrder.getSide()));

    }

    @Test
    public void testAllPendingOrdersForAccount() {
        OperationResultContext<com.tradebot.core.order.Order<String>> pendingOrder =
            bitmexOrderManagementProviderSpy.pendingOrderForAccount(newOrder.getOrderID(), newOrder.getAccount().longValue());

        assertThat(pendingOrder.getData().getOrderId()).isEqualTo(newOrder.getOrderID());
        assertThat(pendingOrder.getData().getInstrument().getInstrument()).isEqualTo(newOrder.getSymbol());
        assertThat(pendingOrder.getData().getSide()).isEqualTo(TradingSignalConvertible.fromString(newOrder.getSide()));
    }

    @Test
    public void testAllPendingOrdersForInstrument() {
        OperationResultContext<Collection<com.tradebot.core.order.Order<String>>> pendingOrders =
            bitmexOrderManagementProviderSpy.pendingOrdersForInstrument(new TradeableInstrument(
                newOrder.getSymbol(), newOrder.getSymbol(), 0.001, null, null, null, null, null));
        assertThat(pendingOrders.getData()).hasSize(1);

        com.tradebot.core.order.Order<String> pendingOrder = pendingOrders.getData().iterator().next();
        assertThat(pendingOrder.getOrderId()).isEqualTo(newOrder.getOrderID());
        assertThat(pendingOrder.getInstrument().getInstrument()).isEqualTo(newOrder.getSymbol());
        assertThat(pendingOrder.getSide()).isEqualTo(TradingSignalConvertible.fromString(newOrder.getSide()));

    }

}
