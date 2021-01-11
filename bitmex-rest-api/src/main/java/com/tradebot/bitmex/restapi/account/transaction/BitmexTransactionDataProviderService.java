package com.tradebot.bitmex.restapi.account.transaction;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.BitmexTransactionTypeEvent;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Transaction;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

@Slf4j
public class BitmexTransactionDataProviderService implements TransactionDataProvider<String, Long, String> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final UserApi userApi = new UserApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    @SneakyThrows
    public com.tradebot.core.account.transaction.Transaction<String, Long, String> getTransaction(String transactionId, Long accountId) {
        return getAllTransaction().stream()
            .filter(transaction -> transaction.getAccount().longValue() == accountId)
            .filter(transaction -> transaction.getTransactID().equals(transactionId))
            .findAny().map(BitmexTransactionDataProviderService::mapToTransaction).orElseThrow();
    }

    @Override
    @SneakyThrows
    public List<com.tradebot.core.account.transaction.Transaction<String, Long, String>> getTransactionsGreaterThanDateTime(
        DateTime dateTime, Long accountId) {
        return getAllTransaction().stream()
            .filter(transaction -> transaction.getAccount().longValue() == accountId)
            .filter(transaction -> dateTime == null || transaction.getTransactTime().compareTo(dateTime) > 0)
            .map(BitmexTransactionDataProviderService::mapToTransaction)
            .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public List<com.tradebot.core.account.transaction.Transaction<String, Long, String>> getTransactionsGreaterThanId(
        String minTransactionId, Long accountId) {

        return getAllTransaction().stream()
            .filter(transaction -> transaction.getAccount().longValue() == accountId)
            .filter(transaction -> transaction.getTransactID().compareTo(minTransactionId) > 0)
            .map(BitmexTransactionDataProviderService::mapToTransaction)
            .collect(Collectors.toList());
    }

    private List<Transaction> getAllTransaction() throws ApiException {
        return getUserApi().userGetWalletHistory(
            bitmexAccountConfiguration.getBitmex().getApi().getMainCurrency(),
            (double) bitmexAccountConfiguration.getBitmex().getApi().getTransactionsDepth(), 0.0);
    }

    private static com.tradebot.core.account.transaction.Transaction<String, Long, String> mapToTransaction(Transaction transaction) {
        return new com.tradebot.core.account.transaction.Transaction<>(
            transaction.getTransactID(),
            BitmexUtils.findByStringMarker(BitmexTransactionTypeEvent.values(),
                bitmexTransactionTypeEvent -> transaction.getTransactType().equals(bitmexTransactionTypeEvent.label())),
            transaction.getAccount().longValue(),
            transaction.getCurrency(),
            transaction.getAmount().longValue(),
            TradingSignal.NONE,
            transaction.getTransactTime(),
            transaction.getAmount().doubleValue(),
            0.0,
            0.0);
    }
}
