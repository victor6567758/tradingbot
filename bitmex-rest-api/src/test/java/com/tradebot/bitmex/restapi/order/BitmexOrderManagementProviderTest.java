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
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.bitmex.restapi.model.OrderStatus;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class BitmexOrderManagementProviderTest {

    private final JSON json = new JSON();
    private final OrderApi orderApi = mock(OrderApi.class);

    private List<Order> orders;
    private Order newOrder;
    private BitmexOrderManagementProvider bitmexOrderManagementProviderSpy;

    @Before
    public void init() throws IOException, ApiException {
        bitmexOrderManagementProviderSpy = spy(new BitmexOrderManagementProvider());
        orders = json.deserialize(Resources.toString(Resources.getResource("ordersAll.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Order>>() {
            }.getType());
        newOrder = orders.stream().filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText())).findAny().orElseThrow();

        doReturn(orders).when(orderApi).orderGetOrders(
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
        Collection<com.tradebot.core.order.Order<String>> pendingOrders = bitmexOrderManagementProviderSpy.allPendingOrders();
        assertThat(pendingOrders).hasSize(1);

        com.tradebot.core.order.Order<String> pendingOrder = pendingOrders.iterator().next();
        assertThat(pendingOrder.getOrderId()).isEqualTo(newOrder.getOrderID());
        assertThat(pendingOrder.getInstrument().getInstrument()).isEqualTo(newOrder.getSymbol());
        assertThat(pendingOrder.getSide()).isEqualTo(TradingSignalConvertible.fromString(newOrder.getSide()));

    }

    @Test
    public void testAllPendingOrdersForAccount() {
        com.tradebot.core.order.Order<String> pendingOrder =
            bitmexOrderManagementProviderSpy.pendingOrderForAccount(newOrder.getOrderID(), newOrder.getAccount().longValue());

        assertThat(pendingOrder.getOrderId()).isEqualTo(newOrder.getOrderID());
        assertThat(pendingOrder.getInstrument().getInstrument()).isEqualTo(newOrder.getSymbol());
        assertThat(pendingOrder.getSide()).isEqualTo(TradingSignalConvertible.fromString(newOrder.getSide()));
    }

    @Test
    public void testAllPendingOrdersForInstrument() {
        Collection<com.tradebot.core.order.Order<String>> pendingOrders =
            bitmexOrderManagementProviderSpy.pendingOrdersForInstrument(new TradeableInstrument(newOrder.getSymbol(), newOrder.getSymbol()));
        assertThat(pendingOrders).hasSize(1);

        com.tradebot.core.order.Order<String> pendingOrder = pendingOrders.iterator().next();
        assertThat(pendingOrder.getOrderId()).isEqualTo(newOrder.getOrderID());
        assertThat(pendingOrder.getInstrument().getInstrument()).isEqualTo(newOrder.getSymbol());
        assertThat(pendingOrder.getSide()).isEqualTo(TradingSignalConvertible.fromString(newOrder.getSide()));

    }

}
