package com.tradebot.core.order;

import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.TradingDecision;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.AccountInfoService;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderExecutionServiceImpl<N, K> extends OrderExecutionServiceBase<N, K> {


    private final AccountInfoService<K> accountInfoService;
    private final BaseTradingConfig baseTradingConfig;
    private final PreOrderValidationService<N, K> preOrderValidationService;
    private final CurrentPriceInfoProvider currentPriceInfoProvider;

    public OrderExecutionServiceImpl(AccountInfoService<K> accountInfoService,
        OrderManagementProvider<N, K> orderManagementProvider,
        BaseTradingConfig baseTradingConfig,
        PreOrderValidationService<N, K> preOrderValidationService,
        CurrentPriceInfoProvider currentPriceInfoProvider,
        OrderExecutionServiceCallback orderExecutionServiceCallback) {

        super(orderExecutionServiceCallback, orderManagementProvider, () -> accountInfoService.findAccountToTrade().orElseThrow());

        this.accountInfoService = accountInfoService;
        this.baseTradingConfig = baseTradingConfig;
        this.preOrderValidationService = preOrderValidationService;
        this.currentPriceInfoProvider = currentPriceInfoProvider;

    }

    @Override
    public List<Order<N>> createOrderListFromDecision(TradingDecision decision) {
        Order<N> order;
        if (decision.getLimitPrice() == 0.0) {
            order = Order.buildMarketOrder(decision.getInstrument(), baseTradingConfig.getMaxAllowedQuantity(),
                decision.getSignal(), decision.getTakeProfitPrice(), decision.getStopLossPrice(), decision.getText());

        } else {
            order = Order.buildLimitOrder(decision.getInstrument(), baseTradingConfig.getMaxAllowedQuantity(),
                decision.getSignal(), decision.getLimitPrice(), decision.getTakeProfitPrice(), decision.getStopLossPrice(), decision.getText());
        }

        return Collections.singletonList(order);
    }

    @Override
    protected boolean preValidate(TradingDecision decision) {
        if (TradingSignal.NONE != decision.getSignal() &&
            preOrderValidationService.checkInstrumentNotAlreadyTraded(decision.getInstrument()) &&
            preOrderValidationService.checkLimitsForCcy(decision.getInstrument(), decision.getSignal())) {

            Price currentPrice = currentPriceInfoProvider.getCurrentPricesForInstrument(decision.getInstrument());

            return preOrderValidationService.isInSafeZone(
                decision.getSignal(),
                decision.getSignal() == TradingSignal.LONG ? currentPrice.getAskPrice()
                    : currentPrice.getBidPrice(), decision.getInstrument());
        }
        return false;
    }


}
