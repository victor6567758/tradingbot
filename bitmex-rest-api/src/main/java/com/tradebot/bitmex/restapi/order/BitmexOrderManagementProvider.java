
package com.tradebot.bitmex.restapi.order;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.bitmex.restapi.utils.converters.OrderTypeConvertible;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.utils.CommonConsts;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class BitmexOrderManagementProvider implements OrderManagementProvider<String, Long> {

    private static final String ORDER_ERROR = "Order error {} {}";
    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexConfiguration();

    private final InstrumentService instrumentService;

    public BitmexOrderManagementProvider(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Getter(AccessLevel.PACKAGE)
    private final OrderApi orderApi = new OrderApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public OperationResultContext<String> placeOrder(com.tradebot.core.order.Order<String> order, Long accountId) {

        try {

            ApiResponse<Order> newOrderResponse = getOrderApi().orderNewWithHttpInfo(
                order.getInstrument().getInstrument(), // symbol
                TradingSignalConvertible.toString(order.getSide()), // side
                null, // simpleOrderQty
                BigDecimal.valueOf(order.getUnits()), // orderQty
                order.getPrice() > 0 ? order.getPrice() : null, // price
                null, // displayQty
                order.getStopPrice() > 0 ? order.getStopPrice() : null, // stopPx
                order.getClientOrderId(), // clOrdID
                null, // clOrdLinkID
                null, // pegOffsetValue
                null, // pegPriceType
                OrderTypeConvertible.toString(order.getType()), // ordType
                null, // timeInForce
                null, // execInst
                null, // contingencyType
                order.getText() // text
            );

            return BitmexUtils.prepareResultReturned(newOrderResponse, new BitmexOperationQuotas<>(newOrderResponse.getData().getOrderID()));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<String> modifyOrder(com.tradebot.core.order.Order<String> order, Long accountId) {

        try {
            ApiResponse<Order> amendedOrderResponse = getOrderApi().orderAmendWithHttpInfo(
                order.getOrderId(),
                null,
                order.getClientOrderId(),
                null,
                BigDecimal.valueOf(order.getUnits()),
                null,
                null,
                order.getPrice() > 0 ? order.getPrice() : null, // price
                order.getStopPrice() > 0 ? order.getStopPrice() : null, // stopPx
                null,
                order.getText()
            );

            return BitmexUtils.prepareResultReturned(amendedOrderResponse, new BitmexOperationQuotas<>(amendedOrderResponse.getData().getOrderID()));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<String> closeOrder(String orderId, Long accountId) {
        try {
            ApiResponse<List<Order>> cancelledResponse = getOrderApi().orderCancelWithHttpInfo(orderId, null, null);

            return cancelledResponse.getData().stream()
                .filter(order -> orderId.equals(order.getOrderID())).findAny()
                .map(cancelledOrder -> BitmexUtils.prepareResultReturned(cancelledResponse,
                    new BitmexOperationQuotas<>(cancelledOrder.getOrderID())))
                .orElseGet(() -> BitmexUtils.prepareResultReturned(cancelledResponse, new BitmexOperationQuotas<>(null)));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<Collection<com.tradebot.core.order.Order<String>>> allPendingOrders() {
        try {

            ApiResponse<List<Order>> apiAllOrdersResponse = getAllOrders();

            List<Order> orders = apiAllOrdersResponse.getData();
            List<com.tradebot.core.order.Order<String>> filteredOrders = orders != null ? applyStandardFilteringCriteria(orders.stream())
                .map(this::toOrder).collect(Collectors.toList()) : null;
            return BitmexUtils.prepareResultReturned(apiAllOrdersResponse, new BitmexOperationQuotas<>(filteredOrders));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }

    }

    @Override
    public OperationResultContext<Collection<com.tradebot.core.order.Order<String>>> pendingOrdersForAccount(Long accountId) {
        try {
            ApiResponse<List<Order>> apiAllOrdersResponse = getAllOrders();

            List<Order> orders = apiAllOrdersResponse.getData();
            List<com.tradebot.core.order.Order<String>> filteredOrders = orders != null ? applyStandardFilteringCriteria(orders.stream())
                .filter(order -> order.getAccount().longValue() == accountId)
                .map(this::toOrder).collect(Collectors.toList()) : null;
            return BitmexUtils.prepareResultReturned(apiAllOrdersResponse, new BitmexOperationQuotas<>(filteredOrders));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<com.tradebot.core.order.Order<String>> pendingOrderForAccount(String orderId, Long accountId) {
        try {
            ApiResponse<List<Order>> apiAllOrdersResponse = getAllOrders();

            List<Order> orders = apiAllOrdersResponse.getData();
            com.tradebot.core.order.Order<String> filteredOrder = orders != null ? applyStandardFilteringCriteria(orders.stream())
                .filter(order -> order.getAccount().longValue() == accountId)
                .filter(order -> order.getOrderID().equals(orderId))
                .findAny().map(this::toOrder).orElse(null) : null;

            return BitmexUtils.prepareResultReturned(apiAllOrdersResponse, new BitmexOperationQuotas<>(filteredOrder));


        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<Collection<com.tradebot.core.order.Order<String>>> pendingOrdersForInstrument(TradeableInstrument instrument) {
        try {

            ApiResponse<List<Order>> apiAllOrdersResponse = getAllOrders();

            List<Order> orders = apiAllOrdersResponse.getData();
            List<com.tradebot.core.order.Order<String>> filteredOrders = orders != null ? applyStandardFilteringCriteria(orders.stream())
                .filter(order -> order.getSymbol().equals(instrument.getInstrument()))
                .map(this::toOrder).collect(Collectors.toList()) : null;
            return BitmexUtils.prepareResultReturned(apiAllOrdersResponse, new BitmexOperationQuotas<>(filteredOrders));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }


    private ApiResponse<List<Order>> getAllOrders() throws ApiException {
        return getOrderApi().orderGetOrdersWithHttpInfo(
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

    private com.tradebot.core.order.Order<String> toOrder(Order order) {

        com.tradebot.core.order.Order<String> convertedOrder = com.tradebot.core.order.Order.<String>builder()
            .instrument(instrumentService.resolveTradeableInstrument(order.getSymbol()))
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

    private static Stream<Order> applyStandardFilteringCriteria(Stream<Order> inputOrderStream) {
        return inputOrderStream.filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
            order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText()));
    }
}
