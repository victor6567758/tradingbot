package com.tradebot.controller.rest;

import com.tradebot.response.CandleResponse;
import com.tradebot.response.GridContextResponse;
import com.tradebot.service.TradingBotRestApi;
import java.util.Collection;
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

  private final TradingBotRestApi tradingBotRestApi;

  @GetMapping("/last")
  public Map<String, GridContextResponse> getLastContextList() {
    return tradingBotRestApi.getLastContextList();
  }

  @GetMapping("/last/{symbol}")
  public List<GridContextResponse> getLastContextList(@PathVariable String symbol) {
    return tradingBotRestApi.getLastContextList(symbol)
        .map(Collections::singletonList).orElse(Collections.emptyList());
  }

  @GetMapping("/history/{symbol}")
  public Set<GridContextResponse> getContextHistory(@PathVariable String symbol) {
    return tradingBotRestApi.getContextHistory(symbol);
  }

  @GetMapping("/candle/{symbol}")
  public Collection<CandleResponse> getCandlestickHistory(@PathVariable String symbol) {
    return tradingBotRestApi.getCandleStickHistory(symbol);
  }

  @GetMapping("/symbols")
  public Set<String> getAllSymbols() {
    return tradingBotRestApi.getAllSymbols();
  }

  @PutMapping("/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetTradingContext() {
    tradingBotRestApi.resetTradingContext();
  }

  @PutMapping("/setTradeEnabled/{enabled}")
  public boolean setTradeEnabled(@PathVariable boolean enabled) {
    return tradingBotRestApi.setGlobalTradesEnabled(enabled);
  }

  @PutMapping("/cancelAllOrders")
  public List<String> cancelAllOrders() {
    return tradingBotRestApi.cancelAllPendingOrders();
  }


}
