package com.tradebot.bitmex.restapi.account.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Transaction;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

public class BitmexTransactionDataProviderServiceTest {

    private final JSON json = new JSON();
    private final UserApi userApi = mock(UserApi.class);
    private BitmexTransactionDataProviderService bitmexTransactionDataProviderServiceSpy;
    private List<Transaction> transactions;

    @Before
    public void init() throws ApiException, IOException {

        bitmexTransactionDataProviderServiceSpy = spy(new BitmexTransactionDataProviderService());

        transactions = json.deserialize(Resources.toString(Resources.getResource("transactionsReply.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Transaction>>() {
            }.getType());

        when(userApi.userGetWalletHistory(anyString(), anyDouble(), anyDouble())).thenReturn(transactions);
        doReturn(userApi).when(bitmexTransactionDataProviderServiceSpy).getUserApi();

    }

    @Test
    public void testGetTransaction() {
        assertThat(transactions.iterator().hasNext()).isTrue();
        Transaction transaction = transactions.iterator().next();

        com.tradebot.core.account.transaction.Transaction<String, Long, String> transactionFound = bitmexTransactionDataProviderServiceSpy
            .getTransaction(transaction.getTransactID(), transaction.getAccount().longValue());

        assertThat(transactionFound.getAccountId()).isEqualTo(transaction.getAccount().longValue());
        assertThat(transactionFound.getPrice()).isCloseTo(transaction.getAmount().doubleValue(), Offset.offset(0.000001));
        assertThat(transactionFound.getTransactionId()).isEqualTo(transaction.getTransactID());
    }

    @Test
    public void testGetTransactionGreaterThanId() {
        assertThat(transactions.iterator().hasNext()).isTrue();
        Transaction transaction = transactions.iterator().next();

        Collection<com.tradebot.core.account.transaction.Transaction<String, Long, String>> transactionsFound =
            bitmexTransactionDataProviderServiceSpy.getTransactionsGreaterThanId(
                "", transaction.getAccount().longValue());
        assertThat(transactionsFound).hasSize(2);

    }

}
