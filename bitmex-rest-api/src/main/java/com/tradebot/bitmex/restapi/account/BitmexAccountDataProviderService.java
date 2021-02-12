package com.tradebot.bitmex.restapi.account;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Margin;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.model.Wallet;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BitmexAccountDataProviderService implements AccountDataProvider<Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final UserApi userApi = new UserApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Getter(AccessLevel.PACKAGE)
    private final PositionApi positionApi = new PositionApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Override
    public Account<Long> getLatestAccountInfo(final Long accountId) {
        return getUserAccount();
    }

    @Override
    public Collection<Account<Long>> getLatestAccountsInfo() {
        return Collections.singletonList(getUserAccount());
    }

    private Account<Long> getUserAccount() {

        try {
            Wallet wallet = getUserApi().userGetWallet(bitmexAccountConfiguration.getBitmex().getApi().getMainCurrency());
            Margin margin = getUserApi().userGetMargin(wallet.getCurrency());

            long totalOpenPositions = getPositionApi().positionGet(null, null, null)
                .stream().filter(Position::isIsOpen)
                .map(position -> position.getCurrentQty().longValue())
                .mapToLong(value -> value).sum();

            return new Account<>(
                margin.getAmount(),
                margin.getUnrealisedPnl(),
                margin.getRealisedPnl(),
                BigDecimal.ZERO,
                margin.getAvailableMargin(),
                totalOpenPositions,
                wallet.getCurrency(),
                wallet.getAccount().longValue(),
                BigDecimal.valueOf(margin.getMarginBalancePcnt()));

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

}
