package com.tradebot.bitmex.restapi.account;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Margin;
import com.tradebot.bitmex.restapi.generated.model.Wallet;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BitmexAccountDataProviderService implements AccountDataProvider<Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final UserApi userApi = new UserApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Override
    @SneakyThrows
    public Account<Long> getLatestAccountInfo(final Long accountId) {
        return getUserAccount();
    }

    @Override
    @SneakyThrows
    public Collection<Account<Long>> getLatestAccountsInfo() {
        return Collections.singletonList(getUserAccount());
    }

    private Account<Long> getUserAccount() throws ApiException {
        Wallet wallet = getUserApi().userGetWallet(bitmexAccountConfiguration.getBitmex().getApi().getMainCurrency());
        Margin margin = getUserApi().userGetMargin(wallet.getCurrency());
        return new Account<>(
            wallet.getAmount().doubleValue(),
            margin.getMarginBalance().doubleValue(),
            wallet.getCurrency(),
            wallet.getAccount().longValue(),
            margin.getMarginLeverage());
    }

}
