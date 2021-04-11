package com.tradebot.core.account.transaction;

import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

@RequiredArgsConstructor
@Slf4j
public class TransactionInfoService<M, N> {

    private static final String INVALID_TRANSACTION_PROVIDER_RESULT_S = "Invalid transaction provider result: %s";

    private final TransactionDataProvider<M, N> transactionDataProvider;
    private final OperationResultCallback operationResultCallback;

    public Transaction<M, N> getTransaction(M transactionId, N accountId) {
        OperationResultContext<Transaction<M, N>> result = transactionDataProvider.getTransaction(transactionId, accountId);
        operationResultCallback.onOperationResult(result);
        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_TRANSACTION_PROVIDER_RESULT_S, result.getMessage()));
    }


    public List<Transaction<M, N>> getTransactionsGreaterThanId(M minTransactionId, N accountId) {
        OperationResultContext<List<Transaction<M, N>>> result = transactionDataProvider.getTransactionsGreaterThanId(minTransactionId, accountId);
        operationResultCallback.onOperationResult(result);
        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_TRANSACTION_PROVIDER_RESULT_S, result.getMessage()));
    }

    public List<Transaction<M, N>> getTransactionsGreaterThanDateTime(DateTime dateTime, N accountId) {
        OperationResultContext<List<Transaction<M, N>>> result = transactionDataProvider
            .getTransactionsGreaterThanDateTime(dateTime, accountId);
        operationResultCallback.onOperationResult(result);
        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_TRANSACTION_PROVIDER_RESULT_S, result.getMessage()));
    }
}
