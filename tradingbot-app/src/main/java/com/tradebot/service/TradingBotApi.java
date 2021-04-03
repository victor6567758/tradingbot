package com.tradebot.service;

import com.tradebot.core.model.OperationResultContext;
import com.tradebot.response.CandleResponse;
import com.tradebot.response.ExecutionResponse;
import com.tradebot.response.MeshResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TradingBotApi {

  Map<String, MeshResponse> getLastMesh();

  Optional<MeshResponse> getLastMesh(String symbol);

  Set<MeshResponse> getMeshHistory(String symbol);

  Set<String> getAllSymbols();

  boolean setGlobalTradesEnabled(boolean tradesEnabledFlag);

  List<String> cancelAllPendingOrders();

  void resetTradingContext();

  Collection<CandleResponse> getCandleStickHistory(String symbol);

  List<ExecutionResponse> getLastExecutionResponseList(String symbol, int level);

  void onOperationResult(OperationResultContext<?> operationResultContext);

}
