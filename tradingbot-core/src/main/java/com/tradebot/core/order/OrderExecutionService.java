package com.tradebot.core.order;

import com.google.common.collect.Lists;
import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.utils.ThreadInterruptedException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderExecutionService<M, N, K> implements Runnable {

    private final BlockingQueue<TradingDecision<N>> orderQueue;
    private final AccountInfoService<K, N> accountInfoService;
    private final OrderManagementProvider<M, N, K> orderManagementProvider;
    private final BaseTradingConfig baseTradingConfig;
    private final PreOrderValidationService<M, N, K> preOrderValidationService;
    private final CurrentPriceInfoProvider<N> currentPriceInfoProvider;
    private volatile boolean serviceUp = true;
    Thread orderExecThread;

    public OrderExecutionService(BlockingQueue<TradingDecision<N>> orderQueue,
        AccountInfoService<K, N> accountInfoService,
        OrderManagementProvider<M, N, K> orderManagementProvider,
        BaseTradingConfig baseTradingConfig,
        PreOrderValidationService<M, N, K> preOrderValidationService,
        CurrentPriceInfoProvider<N> currentPriceInfoProvider) {
        this.orderQueue = orderQueue;
        this.accountInfoService = accountInfoService;
        this.orderManagementProvider = orderManagementProvider;
        this.baseTradingConfig = baseTradingConfig;
        this.preOrderValidationService = preOrderValidationService;
        this.currentPriceInfoProvider = currentPriceInfoProvider;
    }

    @PostConstruct
    public void init() {
        orderExecThread = new Thread(this, this.getClass().getSimpleName());
        orderExecThread.start();
    }

    @PreDestroy
    public void shutDown() {
        this.serviceUp = false;
    }

    @Override
    public void run() {
        while (serviceUp) {
            try {
                TradingDecision<N> decision = this.orderQueue.take();
                if (!preValidate(decision)) {
                    continue;
                }
                Collection<K> accountIds = this.accountInfoService.findAccountsToTrade();
                if (accountIds.isEmpty()) {
                    log.info(
                        "Not a single eligible account found as the reserve may have been exhausted.");
                    continue;
                }
                Order<N, M> order = null;
                if (decision.getLimitPrice() == 0.0) {
                    // market order
                    order = new Order<>(decision.getInstrument(),
                        this.baseTradingConfig.getMaxAllowedQuantity(),
                        decision.getSignal(), OrderType.MARKET, decision.getTakeProfitPrice(),
                        decision
                            .getStopLossPrice());
                } else {
                    order = new Order<>(decision.getInstrument(),
                        this.baseTradingConfig.getMaxAllowedQuantity(),
                        decision.getSignal(), OrderType.LIMIT, decision.getTakeProfitPrice(),
                        decision.getStopLossPrice(), decision.getLimitPrice());
                }
                for (K accountId : accountIds) {
                    M orderId = this.orderManagementProvider.placeOrder(order, accountId);
                    if (orderId != null) {
                        order.setOrderId(orderId);
                    }
                    break;
                }
            } catch (RuntimeException runtimeException) {
                log.error("Runtime error encountered inside order execution service",
                    runtimeException);
            } catch (InterruptedException interruptedException) {
                throw new ThreadInterruptedException(interruptedException);
            }
        }
    }

    private boolean preValidate(TradingDecision<N> decision) {
        if (TradingSignal.NONE != decision.getSignal()
            && this.preOrderValidationService
            .checkInstrumentNotAlreadyTraded(decision.getInstrument())
            && this.preOrderValidationService
            .checkLimitsForCcy(decision.getInstrument(), decision.getSignal())) {
            Collection<TradeableInstrument<N>> instruments = Lists.newArrayList();
            instruments.add(decision.getInstrument());
            Map<TradeableInstrument<N>, Price<N>> priceMap = this.currentPriceInfoProvider
                .getCurrentPricesForInstruments(instruments);
            if (priceMap.containsKey(decision.getInstrument())) {
                Price<N> currentPrice = priceMap.get(decision.getInstrument());
                return this.preOrderValidationService.isInSafeZone(decision.getSignal(),
                    decision.getSignal() == TradingSignal.LONG ? currentPrice.getAskPrice()
                        : currentPrice
                            .getBidPrice(), decision.getInstrument());
            }
        }
        return false;
    }


}
