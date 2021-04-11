package com.tradebot.bitmex.restapi.account.transaction;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.BitmexTransactionTypeEvent;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Transaction;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.TradingSignal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

@Slf4j
public class BitmexTransactionDataProviderService implements TransactionDataProvider<String, Long> {

    private static final String TRANSACTION_PROVIDER_ERROR = "Transaction provider error {} {}";

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexConfiguration();

    private final InstrumentService instrumentService;

    public BitmexTransactionDataProviderService(InstrumentService instrumentService) {
        Assert.notNull(instrumentService, "InstrumentService cannot be null");
        this.instrumentService = instrumentService;
    }

    @Getter(AccessLevel.PACKAGE)
    private final UserApi userApi = new UserApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public OperationResultContext<com.tradebot.core.account.transaction.Transaction<String, Long>> getTransaction(
        String transactionId, Long accountId) {

        try {
            ApiResponse<List<Transaction>> apiResponse = getAllTransaction();
            List<Transaction> transactions = apiResponse.getData();

            com.tradebot.core.account.transaction.Transaction<String, Long> filteredTransaction =
                transactions != null ? transactions.stream()
                    .filter(transaction -> transaction.getAccount().longValue() == accountId)
                    .filter(transaction -> transaction.getTransactID().equals(transactionId))
                    .findAny().map(this::mapToTransaction).orElseThrow() : null;

            return BitmexUtils.prepareResultReturned(apiResponse, new BitmexOperationQuotas<>(filteredTransaction));


        } catch (ApiException apiException) {
            log.error(TRANSACTION_PROVIDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<List<com.tradebot.core.account.transaction.Transaction<String, Long>>> getTransactionsGreaterThanDateTime(
        DateTime dateTime, Long accountId) {

        try {
            ApiResponse<List<Transaction>> apiResponse = getAllTransaction();
            List<Transaction> transactions = apiResponse.getData();

            List<com.tradebot.core.account.transaction.Transaction<String, Long>> filteredTransactions =
                transactions != null ? transactions.stream()
                    .filter(transaction -> transaction.getAccount().longValue() == accountId)
                    .filter(transaction -> {
                        if (dateTime == null || transaction.getTransactTime() == null) {
                            return true;
                        } else {
                            return transaction.getTransactTime().compareTo(dateTime) > 0;
                        }
                    })
                    .map(this::mapToTransaction)
                    .collect(Collectors.toList()) : null;

            return BitmexUtils.prepareResultReturned(apiResponse, new BitmexOperationQuotas<>(filteredTransactions));


        } catch (ApiException apiException) {
            log.error(TRANSACTION_PROVIDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<List<com.tradebot.core.account.transaction.Transaction<String, Long>>> getTransactionsGreaterThanId(
        String minTransactionId, Long accountId) {

        try {
            ApiResponse<List<Transaction>> apiResponse = getAllTransaction();
            List<Transaction> transactions = apiResponse.getData();

            List<com.tradebot.core.account.transaction.Transaction<String, Long>> filteredTransactions =
                transactions != null ? transactions.stream()
                    .filter(transaction -> transaction.getAccount().longValue() == accountId)
                    .filter(transaction -> transaction.getTransactID().compareTo(minTransactionId) > 0)
                    .map(this::mapToTransaction)
                    .collect(Collectors.toList()) : null;

            return BitmexUtils.prepareResultReturned(apiResponse, new BitmexOperationQuotas<>(filteredTransactions));


        } catch (ApiException apiException) {
            log.error(TRANSACTION_PROVIDER_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }

    }

    private ApiResponse<List<Transaction>> getAllTransaction() throws ApiException {
        return getUserApi().userGetWalletHistoryWithHttpInfo(
            bitmexAccountConfiguration.getBitmex().getApi().getMainCurrency(),
            (double) bitmexAccountConfiguration.getBitmex().getApi().getTransactionsDepth(), 0.0);
    }

    private com.tradebot.core.account.transaction.Transaction<String, Long> mapToTransaction(Transaction transaction) {
        return new com.tradebot.core.account.transaction.Transaction<>(
            transaction.getTransactID(),
            BitmexUtils.findByStringMarker(
                BitmexTransactionTypeEvent.values(),
                bitmexTransactionTypeEvent -> transaction.getTransactType().equals(bitmexTransactionTypeEvent.label())),
            transaction.getAccount().longValue(),
            StringUtils.isNotBlank(transaction.getAddress()) ?
                instrumentService.resolveTradeableInstrument(transaction.getAddress()) : null,
            transaction.getAmount().longValue(),
            TradingSignal.NONE,
            transaction.getTransactTime(),
            transaction.getAmount().doubleValue(),
            0.0,
            0.0);
    }
}
