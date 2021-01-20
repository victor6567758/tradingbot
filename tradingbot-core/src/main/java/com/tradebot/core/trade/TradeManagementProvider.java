
package com.tradebot.core.trade;

import java.util.Collection;


public interface TradeManagementProvider<M, K> {

    boolean modifyTrade(K accountId, M tradeId, double stopLoss, double takeProfit);

    boolean closeTrade(M tradeId, K accountId);

    Trade<M, K> getTradeForAccount(M tradeId, K accountId);

    Collection<Trade<M, K>> getTradesForAccount(K accountId);
}
