package com.tradebot.service;

import com.tradebot.response.CandleResponse;
import com.tradebot.response.GridContextResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TradingBotRestApi {

  Map<String, GridContextResponse> getLastContextList();

  Optional<GridContextResponse> getLastContextList(String symbol);

  Set<GridContextResponse> getContextHistory(String symbol);

  Set<String> getAllSymbols();

  boolean setGlobalTradesEnabled(boolean tradesEnabledFlag);

  List<String> cancelAllPendingOrders();

  void resetTradingContext();

  Collection<CandleResponse> getCandleStickHistory(String symbol);

}
