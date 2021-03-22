package com.tradebot.core.account;

import com.tradebot.core.model.BaseTradingConfig;
import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AccountInfoService<K> {

    private static final String INVALID_ACCOUNT_PROVIDER_RESULT_S = "Invalid order provider result: %s";

    private final AccountDataProvider<K> accountDataProvider;
    private final BaseTradingConfig baseTradingConfig;
    private final OperationResultCallback operationResultCallback;

    public Collection<Account<K>> getAllAccounts() {
        OperationResultContext<Collection<Account<K>>> result = accountDataProvider.getLatestAccountsInfo();
        operationResultCallback.onOrderResult(result);
        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ACCOUNT_PROVIDER_RESULT_S, result.getMessage()));
    }

    public Account<K> getAccountInfo(K accountId) {
        OperationResultContext<Account<K>> result = accountDataProvider.getLatestAccountInfo(accountId);
        operationResultCallback.onOrderResult(result);
        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_ACCOUNT_PROVIDER_RESULT_S, result.getMessage()));
    }

    public Collection<K> findAccountsToTrade() {
        return getAllAccounts().stream()
            .sorted(Comparator.comparingDouble(account -> account.getMarginAvailable().doubleValue())).filter(
                account ->
                    account.getAmountAvailableRatio().doubleValue() >= baseTradingConfig.getMinReserveRatio()
                        && account.getNetAssetValue().doubleValue() >= baseTradingConfig.getMinAmountRequired()
            ).map(Account::getAccountId).collect(Collectors.toList());
    }

    public Optional<K> findAccountToTrade() {
        return getAllAccounts().stream()
            .sorted(Comparator.comparingDouble(account -> account.getMarginAvailable().doubleValue())).filter(
                account ->
                    account.getAmountAvailableRatio().doubleValue() >= baseTradingConfig.getMinReserveRatio()
                        && account.getNetAssetValue().doubleValue() >= baseTradingConfig.getMinAmountRequired()
            ).map(Account::getAccountId).findFirst();
    }

}
