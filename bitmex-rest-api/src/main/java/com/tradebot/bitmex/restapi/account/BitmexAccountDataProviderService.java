package com.tradebot.bitmex.restapi.account;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Margin;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.model.Wallet;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.model.OperationResultContext;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;


@Slf4j
public class BitmexAccountDataProviderService implements AccountDataProvider<Long> {

    private static final String ACCOUNT_ERROR = "Position error {} {}";

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexConfiguration();

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
    public OperationResultContext<Account<Long>> getLatestAccountInfo(final Long accountId) {
        return getUserAccount();
    }

    @Override
    public OperationResultContext<Collection<Account<Long>>> getLatestAccountsInfo() {
        OperationResultContext<Account<Long>> userAccountContext = getUserAccount();
        return userAccountContext.isResult() ? new OperationResultContext<>(Collections.singletonList(userAccountContext.getData())) :
            new OperationResultContext<>(Collections.emptyList(), userAccountContext.getMessage());
    }

    private OperationResultContext<Account<Long>> getUserAccount() {

        try {
            ApiResponse<Wallet> walletResponse = getUserApi().userGetWalletWithHttpInfo(bitmexAccountConfiguration.getBitmex().getApi().getMainCurrency());
            Wallet wallet = walletResponse.getData();

            if (wallet == null) {
                throw new IllegalArgumentException(String.format("Cannot resolve wallet information: %d", walletResponse.getStatusCode()));
            }

            // we intentionally do not check walletResponse time limits for simplicity
            ApiResponse<Margin> marginResponse = getUserApi().userGetMarginWithHttpInfo(wallet.getCurrency());
            Margin margin = marginResponse.getData();

            long totalOpenPositions = getPositionApi().positionGet(null, null, null)
                .stream().filter(Position::isIsOpen)
                .map(position -> position.getCurrentQty().longValue())
                .mapToLong(value -> value).sum();

            Account<Long> account = new Account<>(
                margin.getAmount(),
                margin.getUnrealisedPnl(),
                margin.getRealisedPnl(),
                BigDecimal.ZERO,
                margin.getAvailableMargin(),
                totalOpenPositions,
                wallet.getCurrency(),
                wallet.getAccount().longValue(),
                BigDecimal.valueOf(margin.getMarginBalancePcnt()));
            return BitmexUtils.prepareResultReturned(marginResponse, new BitmexOperationQuotas<>(account));

        } catch (ApiException apiException) {
            log.error(ACCOUNT_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

}
