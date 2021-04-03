package com.tradebot.controller.rest;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.order.Order;
import com.tradebot.core.utils.CommonConsts;
import com.tradebot.request.LimitOrderRequest;
import com.tradebot.request.MarketOrderRequest;
import com.tradebot.service.BitmexOrderManager;
import com.tradebot.service.TradingBotApi;
import javax.annotation.PostConstruct;
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

    private final InstrumentDataProvider instrumentDataProvider;

    private final BitmexOrderManager bitmexOrderManager;

    private final BitmexAccountConfiguration bitmexAccountConfiguration;

    private final TradingBotApi tradingBotRestApi;

    private InstrumentService instrumentService;

    @PostConstruct
    public void initialize() {
        bitmexOrderManager.initialize(-1L, bitmexAccountConfiguration);
        instrumentService = new InstrumentService(instrumentDataProvider, tradingBotRestApi::onOperationResult);
    }

    @PutMapping("/openLimitTrade")
    @ResponseStatus(HttpStatus.CREATED)
    public void openLimitOrder(@RequestBody LimitOrderRequest limitOrderRequest) {
        Order<String> order = Order.buildLimitOrder(
            instrumentService.resolveTradeableInstrument(limitOrderRequest.getSymbol()),
            limitOrderRequest.getLots(),
            TradingSignalConvertible.fromString(limitOrderRequest.getTradingSignal()),
            limitOrderRequest.getLimitPrice(),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE);

        bitmexOrderManager.submitOrder(order);
    }

    @PutMapping("/openMarketTrade")
    @ResponseStatus(HttpStatus.CREATED)
    public void openLimitOrder(@RequestBody MarketOrderRequest marketOrderRequest) {
        Order<String> order = Order.buildMarketOrder(
            instrumentService.resolveTradeableInstrument(marketOrderRequest.getSymbol()),
            marketOrderRequest.getLots(),
            TradingSignalConvertible.fromString(marketOrderRequest.getTradingSignal()),
            CommonConsts.INVALID_PRICE,
            CommonConsts.INVALID_PRICE);

        bitmexOrderManager.submitOrder(order);
    }
}
