
package com.tradebot.core.trade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.utils.TradingUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;


@Slf4j
@RequiredArgsConstructor
public class TradeInfoService<M, N, K> {

    private final TradeManagementProvider<M, N, K> tradeManagementProvider;
    private final AccountDataProvider<K> accountDataProvider;

    private final ConcurrentMap<K, Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>>> tradesCache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    public Collection<K> findAllAccountsWithInstrumentTrades(TradeableInstrument<N> instrument) {
        lock.readLock().lock();
        try {
            return tradesCache.keySet().stream()
                .filter(accountId -> isTradeExistsForInstrument(instrument, accountId))
                .collect(Collectors.toList());

        } finally {
            lock.readLock().unlock();
        }
    }

    @PostConstruct
    public void init() {
        reconstructCache();
    }

    private void reconstructCache() {
        lock.writeLock().lock();
        try {
            tradesCache.clear();
            for (Account<K> account : accountDataProvider.getLatestAccountsInfo()) {
                tradesCache.put(account.getAccountId(), getTradesPerInstrumentForAccount(
                    account.getAccountId()));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int findNetPositionCountForCurrency(String currency) {
        lock.readLock().lock();
        try {
            return tradesCache.keySet().stream()
                .mapToInt(accountId -> findNetPositionCountForCurrency(currency, accountId)).sum();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<Trade<M, N, K>> getAllTrades() {
        lock.readLock().lock();
        try {
            Collection<Trade<M, N, K>> trades = new ArrayList<>();
            for (K accId : tradesCache.keySet()) {
                trades.addAll(getTradesForAccount(accId));
            }
            return trades;
        } finally {
            lock.readLock().unlock();
        }

    }

    public Collection<Trade<M, N, K>> getTradesForAccountAndInstrument(K accountId,
        TradeableInstrument<N> instrument) {
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradesForAccount = tradesCache
            .get(accountId);
        if (!MapUtils.isEmpty(tradesForAccount) && tradesForAccount
            .containsKey(instrument)) {
            return Lists.newArrayList(tradesForAccount.get(instrument));
        }
        return Collections.emptyList();
    }


    public Collection<Trade<M, N, K>> getTradesForAccount(K accountId) {
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradesForAccount = tradesCache
            .get(accountId);
        Collection<Trade<M, N, K>> trades = Lists.newArrayList();
        if (MapUtils.isEmpty(tradesForAccount)) {
            return trades;
        }
        for (Collection<Trade<M, N, K>> tradeLst : tradesForAccount.values()) {
            trades.addAll(tradeLst);
        }
        return trades;
    }


    public void refreshTradesForAccount(K accountId) {
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = getTradesPerInstrumentForAccount(
            accountId);
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> oldTradeMap = tradesCache
            .get(accountId);
        oldTradeMap.clear();
        oldTradeMap.putAll(tradeMap);
    }


    public boolean isTradeExistsForInstrument(TradeableInstrument<N> instrument) {
        lock.readLock().lock();
        try {
            return tradesCache.keySet().stream()
                .anyMatch(accountId -> isTradeExistsForInstrument(instrument, accountId));
        } finally {
            lock.readLock().unlock();
        }
    }


    private Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> getTradesPerInstrumentForAccount(
        K accountId) {
        Collection<Trade<M, N, K>> trades = tradeManagementProvider.getTradesForAccount(accountId);
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = Maps.newHashMap();
        for (Trade<M, N, K> ti : trades) {
            Collection<Trade<M, N, K>> tradeLst = null;
            if (tradeMap.containsKey(ti.getInstrument())) {
                tradeLst = tradeMap.get(ti.getInstrument());
            } else {
                tradeLst = Lists.newArrayList();
                tradeMap.put(ti.getInstrument(), tradeLst);
            }
            tradeLst.add(ti);
        }
        return tradeMap;
    }


    private int findNetPositionCountForCurrency(String currency, K accountId) {
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradeMap = tradesCache
            .get(accountId);
        if (MapUtils.isEmpty(tradeMap)) {
            return 0;
        } else {
            int positionCtr = 0;
            for (Collection<Trade<M, N, K>> trades : tradeMap.values()) {
                for (Trade<M, N, K> tradeInfo : trades) {
                    positionCtr += TradingUtils
                        .getSign(tradeInfo.getInstrument().getInstrument(), tradeInfo
                            .getSide(), currency);
                }
            }

            return positionCtr;
        }
    }


    private boolean isTradeExistsForInstrument(TradeableInstrument<N> instrument, K accountId) {
        Map<TradeableInstrument<N>, Collection<Trade<M, N, K>>> tradesForAccount =
            tradesCache.get(accountId);
        if (MapUtils.isEmpty(tradesForAccount)) {
            return false;
        }
        return tradesForAccount.containsKey(instrument);

    }
}
