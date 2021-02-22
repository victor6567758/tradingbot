
package com.tradebot.bitmex.restapi.order;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOrderQuotas;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.bitmex.restapi.utils.converters.OrderTypeConvertible;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.utils.CommonConsts;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;

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
    public OrderResultContext<String> placeOrder(com.tradebot.core.order.Order<String> order, Long accountId) {

        try {

            ApiResponse<Order> newOrder = getOrderApi().orderNewWithHttpInfo(
                order.getInstrument().getInstrument(), // symbol
                TradingSignalConvertible.toString(order.getSide()), // side
                null, // simpleOrderQty
                BigDecimal.valueOf(order.getUnits()), // orderQty
                order.getPrice() > 0 ? order.getPrice() : null, // price
                BigDecimal.valueOf(0), // displayQty
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

            if (newOrder.getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("Open order returned wrong HTTP code");
            }
            return prepareResult(newOrder);

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(order.getInstrument().getInstrument(), apiException);
        }
    }

    @Override
    public OrderResultContext<String> modifyOrder(com.tradebot.core.order.Order<String> order, Long accountId) {

        try {
            ApiResponse<Order> amendedOrder = getOrderApi().orderAmendWithHttpInfo(
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

            if (amendedOrder.getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("Amend order returned wrong HTTP code");
            }
            return prepareResult(amendedOrder);

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(order.getInstrument().getInstrument(), apiException);
        }
    }

    @Override
    public OrderResultContext<String> closeOrder(String orderId, Long accountId) {
        try {
            ApiResponse<List<Order>> cancelled = getOrderApi().orderCancelWithHttpInfo(orderId, null, null);
            if (cancelled.getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("Amend order returned wrong HTTP code");
            }

            Order cancelledOrder =
                cancelled.getData().stream().filter(order -> orderId.equals(order.getOrderID())).findAny().orElse(null);

            return cancelledOrder != null ? new OrderResultContext<>(orderId, cancelledOrder.getSymbol()) :
                new OrderResultContext<>(orderId, null, "No order was cancelled");

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(null, apiException);
        }
    }

    @Override
    public OrderResultContext<Collection<com.tradebot.core.order.Order<String>>> allPendingOrders() {
        try {
            return new OrderResultContext<>(getAllOrders().stream().filter(
                order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
            ).map(this::toOrder).collect(Collectors.toList()));
        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(null, apiException);
        }

    }

    @Override
    public OrderResultContext<Collection<com.tradebot.core.order.Order<String>>> pendingOrdersForAccount(Long accountId) {
        try {
            return new OrderResultContext<>(getAllOrders().stream()
                .filter(order -> order.getAccount().longValue() == accountId)
                .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
                ).map(this::toOrder).collect(Collectors.toList()));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(null, apiException);
        }
    }

    @Override
    public OrderResultContext<com.tradebot.core.order.Order<String>> pendingOrderForAccount(String orderId, Long accountId) {
        try {
            return new OrderResultContext<>(getAllOrders().stream()
                .filter(order -> order.getAccount().longValue() == accountId)
                .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
                ).filter(order -> order.getOrderID().equals(orderId))
                .map(this::toOrder).findAny().orElseThrow());

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(null, apiException);
        }
    }

    @Override
    public OrderResultContext<Collection<com.tradebot.core.order.Order<String>>> pendingOrdersForInstrument(TradeableInstrument instrument) {
        try {
            return new OrderResultContext<>(getAllOrders().stream()
                .filter(order -> order.getSymbol().equals(instrument.getInstrument()))
                .filter(order -> order.getOrdStatus().equals(OrderStatus.NEW.getStatusText()) ||
                    order.getOrdStatus().equals(OrderStatus.PARTIALLY_FILLED.getStatusText())
                ).map(this::toOrder).collect(Collectors.toList()));

        } catch (ApiException apiException) {
            log.error(ORDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return prepareErrorResult(null, apiException);
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

    private com.tradebot.core.order.Order<String> toOrder(Order order) {
        TradeableInstrument instrument = instrumentService.resolveTradeableInstrument(order.getSymbol());
        com.tradebot.core.order.Order<String> convertedOrder = com.tradebot.core.order.Order.<String>builder()
            .instrument(instrument)
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

    private BitmexOrderQuotas prepareResult(ApiResponse<Order> apiResponse) {
        BitmexOrderQuotas result = new BitmexOrderQuotas(apiResponse.getData().getOrderID(), apiResponse.getData().getSymbol());
        result.setXRatelimitLimit(getIntHeaderValue("x-ratelimit-limit", apiResponse.getHeaders()));
        result.setXRatelimitRemaining(getIntHeaderValue("x-ratelimit-remaining", apiResponse.getHeaders()));
        result.setXRatelimitReset(getIntHeaderValue("x-ratelimit-reset", apiResponse.getHeaders()));
        result.setXRatelimitRemaining1s(getIntHeaderValue("x-ratelimit-remaining-1s", apiResponse.getHeaders()));

        return result;
    }

    private static <RT> OrderResultContext<RT> prepareErrorResult(String symbol, ApiException apiException) {
        return new OrderResultContext<>(null, symbol, BitmexUtils.errorMessageFromApiException(apiException));
    }

    private static int getIntHeaderValue(String name, Map<String, List<String>> headers) {
        return NumberUtils.toInt(headers.getOrDefault(name, Collections.singletonList("-1")).get(0), -1);
    }


}
