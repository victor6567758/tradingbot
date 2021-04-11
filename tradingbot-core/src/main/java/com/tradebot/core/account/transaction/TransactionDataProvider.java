package com.tradebot.core.account.transaction;

import com.tradebot.core.model.OperationResultContext;
import java.util.List;
import org.joda.time.DateTime;

public interface TransactionDataProvider<M, N> {

    OperationResultContext<Transaction<M, N>> getTransaction(M transactionId, N accountId);

    OperationResultContext<List<Transaction<M, N>>> getTransactionsGreaterThanId(M minTransactionId, N accountId);

    OperationResultContext<List<Transaction<M, N>>> getTransactionsGreaterThanDateTime(DateTime dateTime, N accountId);
}
