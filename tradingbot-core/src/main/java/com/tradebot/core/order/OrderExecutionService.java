package com.tradebot.core.order;

import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.utils.CommonUtils;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderExecutionService<M, N, K> {

    private static final long SHUTDOWN_WAIT_TIME = 5000L;

    private final AccountInfoService<K, N> accountInfoService;
    private final OrderManagementProvider<M, N, K> orderManagementProvider;
    private final BaseTradingConfig baseTradingConfig;
    private final PreOrderValidationService<M, N, K> preOrderValidationService;
    private final CurrentPriceInfoProvider<N> currentPriceInfoProvider;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // TODO track 60 submissions per miniute

    public OrderExecutionService(AccountInfoService<K, N> accountInfoService,
        OrderManagementProvider<M, N, K> orderManagementProvider,
        BaseTradingConfig baseTradingConfig,
        PreOrderValidationService<M, N, K> preOrderValidationService,
        CurrentPriceInfoProvider<N> currentPriceInfoProvider) {

        this.accountInfoService = accountInfoService;
        this.orderManagementProvider = orderManagementProvider;
        this.baseTradingConfig = baseTradingConfig;
        this.preOrderValidationService = preOrderValidationService;
        this.currentPriceInfoProvider = currentPriceInfoProvider;
    }

    @PreDestroy
    public void shutdown() {
        CommonUtils.commonExecutorServiceShutdown(executorService, SHUTDOWN_WAIT_TIME);
    }

    public Future<Boolean> submit(TradingDecision<N> decision) {
        return executorService.submit(() -> processTradingDecision(decision), true);
    }

    private void processTradingDecision(TradingDecision<N> decision) {
        try {
            if (!preValidate(decision)) {
                return;
            }
            Optional<K> accountId = this.accountInfoService.findAccountToTrade();
            if (accountId.isEmpty()) {
                log.info("Not a single eligible account found as the reserve may have been exhausted.");
                return;
            }
            Order<N, M> order;
            if (decision.getLimitPrice() == 0.0) {
                order = Order.buildMarketOrder(decision.getInstrument(), baseTradingConfig.getMaxAllowedQuantity(),
                    decision.getSignal(), decision.getTakeProfitPrice(), decision.getStopLossPrice());

            } else {
                order = Order.buildLimitOrder(decision.getInstrument(), baseTradingConfig.getMaxAllowedQuantity(),
                    decision.getSignal(), decision.getLimitPrice(), decision.getTakeProfitPrice(), decision.getStopLossPrice());
            }
            M orderId = orderManagementProvider.placeOrder(order, accountId.get());
            if (orderId != null) {
                order.setOrderId(orderId);
            }
        } catch (RuntimeException runtimeException) {
            log.error("Runtime error encountered inside order execution service", runtimeException);
        }
    }


    private boolean preValidate(TradingDecision<N> decision) {
        if (TradingSignal.NONE != decision.getSignal() &&
            preOrderValidationService.checkInstrumentNotAlreadyTraded(decision.getInstrument()) &&
            preOrderValidationService
                .checkLimitsForCcy(decision.getInstrument(), decision.getSignal())) {

            Price<N> currentPrice = currentPriceInfoProvider
                .getCurrentPricesForInstrument(decision.getInstrument());

            return preOrderValidationService.isInSafeZone(
                decision.getSignal(),
                decision.getSignal() == TradingSignal.LONG ? currentPrice.getAskPrice()
                    : currentPrice.getBidPrice(), decision.getInstrument());
        }
        return false;
    }


}
