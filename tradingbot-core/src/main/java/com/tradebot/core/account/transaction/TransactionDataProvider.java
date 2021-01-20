package com.tradebot.core.account.transaction;

import java.util.List;
import org.joda.time.DateTime;

public interface TransactionDataProvider<M, N> {

    Transaction<M, N> getTransaction(M transactionId, N accountId);

    List<Transaction<M, N>> getTransactionsGreaterThanId(M minTransactionId, N accountId);

    List<Transaction<M, N>> getTransactionsGreaterThanDateTime(DateTime dateTime, N accountId);
}
