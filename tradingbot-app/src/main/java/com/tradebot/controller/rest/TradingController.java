package com.tradebot.controller.rest;

import com.tradebot.response.CandleResponse;
import com.tradebot.response.MeshResponse;
import com.tradebot.service.TradingBotApi;
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

  private final TradingBotApi tradingBotRestApi;

  @GetMapping("/last")
  public Map<String, MeshResponse> getLastMesh() {
    return tradingBotRestApi.getLastMesh();
  }

  @GetMapping("/last/{symbol}")
  public List<MeshResponse> getLastMesh(@PathVariable String symbol) {
    return tradingBotRestApi.getLastMesh(symbol)
        .map(Collections::singletonList).orElse(Collections.emptyList());
  }

  @GetMapping("/history/{symbol}")
  public Set<MeshResponse> getMeshHistory(@PathVariable String symbol) {
    return tradingBotRestApi.getMeshHistory(symbol);
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
