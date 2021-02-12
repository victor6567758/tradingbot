
package com.tradebot.bitmex.restapi.order;

import com.tradebot.bitmex.restapi.BitmexConstants;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class BitmexOrderManagementProvider implements OrderManagementProvider<String, Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final OrderApi orderApi = new OrderApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public String placeOrder(com.tradebot.core.order.Order<String> order, Long accountId) {

        try {
            Order newOrder = getOrderApi().orderNew(
                order.getInstrument().getInstrument(), // symbol
                TradingSignalConvertible.toString(order.getSide()), // side
                null, // simpleOrderQty
                BigDecimal.valueOf(order.getUnits()), // orderQty
                order.getPrice() > 0 ? order.getPrice() : null, // price
                BigDecimal.valueOf(0), // displayQty
                order.getStopPrice() > 0 ? order.getStopPrice() : null, // stopPx
                null, // clOrdID
                null, // clOrdLinkID
                null, // pegOffsetValue
                null, // pegPriceType
                OrderTypeConvertible.toString(order.getType()), // ordType
                null, // timeInForce
                null, // execInst
                null, // contingencyType
                null // text
            );

            return newOrder.getOrderID();

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public boolean modifyOrder(com.tradebot.core.order.Order<String> order, Long accountId) {

        try {
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

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public boolean closeOrder(String orderId, Long accountId) {
        try {
            List<Order> cancelled = getOrderApi().orderCancel(orderId, null, null);
            return cancelled.stream().anyMatch(order -> order.getOrderID().equals(orderId));

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public Collection<com.tradebot.core.order.Order<String>> allPendingOrders() {
        try {
            return getAllOrders().stream().filter(
                order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
            ).map(BitmexOrderManagementProvider::toOrder).collect(Collectors.toList());
        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }

    }

    @Override
    public Collection<com.tradebot.core.order.Order<String>> pendingOrdersForAccount(Long accountId) {
        try {
            return getAllOrders().stream()
                .filter(order -> order.getAccount().longValue() == accountId)
                .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
                ).map(BitmexOrderManagementProvider::toOrder).collect(Collectors.toList());

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public com.tradebot.core.order.Order<String> pendingOrderForAccount(String orderId, Long accountId) {
        try {
            return getAllOrders().stream()
                .filter(order -> order.getAccount().longValue() == accountId)
                .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
                ).filter(order -> order.getOrderID().equals(orderId))
                .map(BitmexOrderManagementProvider::toOrder).findAny().orElseThrow();

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public Collection<com.tradebot.core.order.Order<String>> pendingOrdersForInstrument(TradeableInstrument instrument) {
        try {
            return getAllOrders().stream()
                .filter(order -> order.getSymbol().equals(instrument.getInstrument()))
                .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
                ).map(BitmexOrderManagementProvider::toOrder).collect(Collectors.toList());

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
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

    private static com.tradebot.core.order.Order<String> toOrder(Order order) {
        com.tradebot.core.order.Order<String> convertedOrder = com.tradebot.core.order.Order.<String>builder()
            .instrument(new TradeableInstrument(order.getSymbol(), order.getSymbol()))
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
