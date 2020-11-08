
package com.tradebot.bitmex.restapi.order;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.model.OrderStatus;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.bitmex.restapi.utils.converters.OrderTypeConvertible;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.utils.CommonConsts;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class BitmexOrderManagementProvider implements OrderManagementProvider<String, String, Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final OrderApi orderApi = new OrderApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    @SneakyThrows
    public String placeOrder(com.tradebot.core.order.Order<String, String> order, Long accountId) {

        Order newOrder = getOrderApi().orderNew(
            order.getInstrument().getInstrument(),
            TradingSignalConvertible.toString(order.getSide()),
            0.0,
            BigDecimal.valueOf(order.getUnits()),
            order.getPrice() > 0 ? order.getPrice() : null,
            BigDecimal.valueOf(order.getUnits()), // TODO - orders are not hidden
            order.getStopPrice() > 0 ? order.getStopPrice() : null,
            null,
            null, // clOrdLinkID
            null,
            null,
            OrderTypeConvertible.toString(order.getType()),
            null,
            null,
            null,
            null
        );

        return newOrder.getOrderID();
    }

    @Override
    @SneakyThrows
    public boolean modifyOrder(com.tradebot.core.order.Order<String, String> order, Long accountId) {
        getOrderApi().orderAmend(
            order.getOrderId(),
            null,
            null,
            null,
            BigDecimal.valueOf(order.getUnits()),
            null,
            null,
            order.getPrice() > 0 ? order.getPrice() : null,
            order.getStopPrice() > 0 ? order.getStopPrice() : null,
            null,
            null
        );
        return true;
    }

    @Override
    @SneakyThrows
    public boolean closeOrder(String orderId, Long accountId) {
        List<Order> cancelled = getOrderApi().orderCancel(orderId, null, null);
        return cancelled.stream().anyMatch(order -> order.getOrderID().equals(orderId));
    }

    @Override
    @SneakyThrows
    public Collection<com.tradebot.core.order.Order<String, String>> allPendingOrders() {
        return getAllOrders().stream().filter(
            order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
        ).map(BitmexOrderManagementProvider::toOrder).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public Collection<com.tradebot.core.order.Order<String, String>> pendingOrdersForAccount(Long accountId) {
        return getAllOrders().stream()
            .filter(order -> order.getAccount().longValue() == accountId)
            .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
            ).map(BitmexOrderManagementProvider::toOrder).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public com.tradebot.core.order.Order<String, String> pendingOrderForAccount(String orderId, Long accountId) {
        return getAllOrders().stream()
            .filter(order -> order.getAccount().longValue() == accountId)
            .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
            ).filter(order -> order.getOrderID().equals(orderId))
            .map(BitmexOrderManagementProvider::toOrder).findAny().orElseThrow();
    }

    @Override
    @SneakyThrows
    public Collection<com.tradebot.core.order.Order<String, String>> pendingOrdersForInstrument(TradeableInstrument<String> instrument) {
        return getAllOrders().stream()
            .filter(order -> order.getSymbol().equals(instrument.getInstrument()))
            .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
            ).map(BitmexOrderManagementProvider::toOrder).collect(Collectors.toList());
    }

    private List<Order> getAllOrders() throws ApiException {
        return getOrderApi().orderGetOrders(
            null,
            null,
            null,
            BigDecimal.valueOf(bitmexAccountConfiguration.getBitmex().getApi().getOrderDepth()),
            BigDecimal.ZERO,
            true,
            null,
            null
        );
    }

    private static com.tradebot.core.order.Order<String, String> toOrder(Order order) {
        com.tradebot.core.order.Order<String, String> convertedOrder = com.tradebot.core.order.Order.<String, String>builder()
            .instrument(new TradeableInstrument<>(order.getSymbol()))
            .units(order.getOrderQty().longValue())
            .side(TradingSignalConvertible.fromString(order.getSide()))
            .type(OrderTypeConvertible.fromString(order.getOrdType()))
            .takeProfit(CommonConsts.INVALID_PRICE)
            .stopLoss(CommonConsts.INVALID_PRICE)
            .price(ObjectUtils.defaultIfNull(order.getPrice(), CommonConsts.INVALID_PRICE))
            .stopPrice(ObjectUtils.defaultIfNull(order.getStopPx(), CommonConsts.INVALID_PRICE)).build();

        convertedOrder.setOrderId(order.getOrderID());
        return convertedOrder;
    }
}
