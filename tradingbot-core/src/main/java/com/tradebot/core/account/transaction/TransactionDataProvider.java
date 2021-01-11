package com.tradebot.core.account.transaction;

import java.util.List;
import org.joda.time.DateTime;

public interface TransactionDataProvider<M, N, T> {

    Transaction<M, N, T> getTransaction(M transactionId, N accountId);

    List<Transaction<M, N, T>> getTransactionsGreaterThanId(M minTransactionId, N accountId);

    List<Transaction<M, N, T>> getTransactionsGreaterThanDateTime(DateTime dateTime, N accountId);
}
