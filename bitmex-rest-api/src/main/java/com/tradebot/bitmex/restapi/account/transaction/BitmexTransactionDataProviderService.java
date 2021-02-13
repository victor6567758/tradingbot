package com.tradebot.bitmex.restapi.account.transaction;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.events.BitmexTransactionTypeEvent;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Transaction;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

@Slf4j
public class BitmexTransactionDataProviderService implements TransactionDataProvider<String, Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    private final InstrumentService instrumentService;

    public BitmexTransactionDataProviderService(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Getter(AccessLevel.PACKAGE)
    private final UserApi userApi = new UserApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public com.tradebot.core.account.transaction.Transaction<String, Long> getTransaction(String transactionId, Long accountId) {

        return getAllTransaction().stream()
            .filter(transaction -> transaction.getAccount().longValue() == accountId)
            .filter(transaction -> transaction.getTransactID().equals(transactionId))
            .findAny().map(this::mapToTransaction).orElseThrow();


    }

    @Override
    public List<com.tradebot.core.account.transaction.Transaction<String, Long>> getTransactionsGreaterThanDateTime(
        DateTime dateTime, Long accountId) {

        return getAllTransaction().stream()
            .filter(transaction -> transaction.getAccount().longValue() == accountId)
            .filter(transaction -> {
                if (dateTime == null || transaction.getTransactTime() == null) {
                    return true;
                } else {
                    return transaction.getTransactTime().compareTo(dateTime) > 0;
                }
            })
            .map(this::mapToTransaction)
            .collect(Collectors.toList());


    }

    @Override
    public List<com.tradebot.core.account.transaction.Transaction<String, Long>> getTransactionsGreaterThanId(
        String minTransactionId, Long accountId) {

        return getAllTransaction().stream()
            .filter(transaction -> transaction.getAccount().longValue() == accountId)
            .filter(transaction -> transaction.getTransactID().compareTo(minTransactionId) > 0)
            .map(this::mapToTransaction)
            .collect(Collectors.toList());

    }

    private List<Transaction> getAllTransaction() {

        try {
            return getUserApi().userGetWalletHistory(
                bitmexAccountConfiguration.getBitmex().getApi().getMainCurrency(),
                (double) bitmexAccountConfiguration.getBitmex().getApi().getTransactionsDepth(), 0.0);

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
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
