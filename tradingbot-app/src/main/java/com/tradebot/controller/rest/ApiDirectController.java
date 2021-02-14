package com.tradebot.controller.rest;

import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderResultContext;
import com.tradebot.core.utils.CommonConsts;
import com.tradebot.request.LimitOrderRequest;
import com.tradebot.request.MarketOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController()
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiDirectController {

    private final OrderManagementProvider<String, Long> orderManagementProvider;

    private final InstrumentService instrumentService;

    @PutMapping("/openLimitTrade")
    public OrderResultContext<String> openLimitOrder(@RequestBody LimitOrderRequest limitOrderRequest) {
        Order<String> order = Order.buildLimitOrder(
            instrumentService.resolveTradeableInstrument(limitOrderRequest.getSymbol()),
            limitOrderRequest.getLots(),
            TradingSignalConvertible.fromString(limitOrderRequest.getTradingSignal()),
            limitOrderRequest.getLimitPrice(),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE);

        return orderManagementProvider.placeOrder(order, -1L);
    }

    @PutMapping("/openMarketTrade")
    public OrderResultContext<String> openLimitOrder(@RequestBody MarketOrderRequest marketOrderRequest) {
        Order<String> order = Order.buildMarketOrder(
            instrumentService.resolveTradeableInstrument(marketOrderRequest.getSymbol()),
            marketOrderRequest.getLots(),
            TradingSignalConvertible.fromString(marketOrderRequest.getTradingSignal()),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE);

        return orderManagementProvider.placeOrder(order, -1L);
    }
}
