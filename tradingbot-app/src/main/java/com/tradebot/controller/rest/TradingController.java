package com.tradebot.controller.rest;

import com.tradebot.response.GridContextResponse;
import com.tradebot.service.BitmexTradingBot;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/signal")
@RequiredArgsConstructor
public class TradingController {

    private final BitmexTradingBot bitmexTradingBot;

    @GetMapping("/last")
    public Map<String, GridContextResponse> getLastContextList() {
        return bitmexTradingBot.getLastContextList();
    }

    @GetMapping("/history")
    public Set<GridContextResponse> getContextHistory() {
        return bitmexTradingBot.getContextHistory();
    }

    @GetMapping("/last/{symbol}")
    public List<GridContextResponse> getLastContextList(@PathVariable String symbol) {
        return bitmexTradingBot.getLastContextList(symbol)
            .map(Collections::singletonList).orElse(Collections.emptyList());
    }

    @GetMapping("/history/{symbol}")
    public Set<GridContextResponse> getContextHistory(@PathVariable String symbol) {
        return bitmexTradingBot.getContextHistory(symbol);
    }

    @GetMapping("/symbols")
    public Set<String> getAllSymbols() {
        return bitmexTradingBot.getAllSymbols();
    }

    @PutMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetTradingContext() {
        bitmexTradingBot.resetTradingContext();
    }

    @PutMapping("/setTradeEnabled/{enabled}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setTradeEnabled(@PathVariable boolean enabled) {
        bitmexTradingBot.setTradesEnabled(enabled);
    }
}
