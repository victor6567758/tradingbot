package com.tradebot.controller.rest;

import com.tradebot.response.GridContextResponse;
import com.tradebot.service.impl.BitmexTradingBotImpl;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/signal")
@RequiredArgsConstructor
public class TradingController {

    private final BitmexTradingBotImpl bitmexTradingBot;

    @GetMapping("/last")
    public Map<String, GridContextResponse> getLastContextList() {
        return bitmexTradingBot.getLastContextList();
    }

    @GetMapping("/history")
    public Map<DateTime, GridContextResponse> getContextHistory() {
        return bitmexTradingBot.getContextHistory();
    }

    @GetMapping("/last/{symbol}")
    public GridContextResponse getLastContextList(@PathVariable String symbol) {
        return bitmexTradingBot.getLastContextList(symbol);
    }

    @GetMapping("/history/{symbol}")
    public Map<DateTime, GridContextResponse> getContextHistory(@PathVariable String symbol) {
        return bitmexTradingBot.getContextHistory(symbol);
    }

    @GetMapping("/symbols")
    public Set<String> getAllSymbols() {
        return bitmexTradingBot.getAllSymbols();
    }


}
