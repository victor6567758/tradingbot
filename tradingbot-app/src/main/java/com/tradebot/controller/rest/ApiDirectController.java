package com.tradebot.controller.rest;

import com.tradebot.bitmex.restapi.order.BitmexOrderManagementProvider;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.utils.CommonConsts;
import com.tradebot.request.LimitOrderRequest;
import com.tradebot.request.MarketOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController()
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiDirectController {

    private final OrderManagementProvider<String, Long> orderManagementProvider = new BitmexOrderManagementProvider();

    @PutMapping("/openLimitTrade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String openLimitOrder(@RequestBody LimitOrderRequest limitOrderRequest) {
        TradeableInstrument instrument = new TradeableInstrument(limitOrderRequest.getSymbol(), limitOrderRequest.getSymbol());
        Order<String> order = Order.buildLimitOrder(
            instrument,
            limitOrderRequest.getLots(),
            TradingSignalConvertible.fromString(limitOrderRequest.getTradingSignal()),
            limitOrderRequest.getLimitPrice(),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE);

        return orderManagementProvider.placeOrder(order, -1L);
    }

    @PutMapping("/openMarketTrade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String openLimitOrder(@RequestBody MarketOrderRequest marketOrderRequest) {
        TradeableInstrument instrument = new TradeableInstrument(marketOrderRequest.getSymbol(), marketOrderRequest.getSymbol());
        Order<String> order = Order.buildMarketOrder(
            instrument,
            marketOrderRequest.getLots(),
            TradingSignalConvertible.fromString(marketOrderRequest.getTradingSignal()),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE);

        return orderManagementProvider.placeOrder(order, -1L);
    }
}
