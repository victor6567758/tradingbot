package com.tradebot.controller.rest;

import com.tradebot.response.GridContextResponse;
import com.tradebot.service.impl.BitmexTradingBotImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/signal")
@RequiredArgsConstructor
public class TradingController {

    private final BitmexTradingBotImpl bitmexTradingBot;

    @GetMapping("/")
    public GridContextResponse getTradingSignals() {
        return null;
    }
}
