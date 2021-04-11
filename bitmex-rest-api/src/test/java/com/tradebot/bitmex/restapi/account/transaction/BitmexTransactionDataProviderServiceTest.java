package com.tradebot.bitmex.restapi.account.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Transaction;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpStatus;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BitmexTransactionDataProviderServiceTest {

    private static final TradeableInstrument XBTUSD_INSTR =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);

    private final JSON json = new JSON();
    private final UserApi userApi = mock(UserApi.class);
    private BitmexTransactionDataProviderService bitmexTransactionDataProviderServiceSpy;
    private InstrumentService instrumentServiceSpy;
    private List<Transaction> transactions;

    @Before
    public void init() throws ApiException, IOException {

        instrumentServiceSpy = mock(InstrumentService.class);
        doReturn(XBTUSD_INSTR).when(instrumentServiceSpy).resolveTradeableInstrument(XBTUSD_INSTR.getInstrument());

        bitmexTransactionDataProviderServiceSpy = spy(new BitmexTransactionDataProviderService(instrumentServiceSpy));

        transactions = json.deserialize(Resources.toString(Resources.getResource("transactionsReply.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Transaction>>() {
            }.getType());

        when(userApi.userGetWalletHistoryWithHttpInfo(anyString(), anyDouble(), anyDouble())).thenReturn(
            new ApiResponse(HttpStatus.SC_OK, Collections.emptyMap(),transactions));
        doReturn(userApi).when(bitmexTransactionDataProviderServiceSpy).getUserApi();

    }

    @Test
    public void testGetTransaction() {
        assertThat(transactions.iterator().hasNext()).isTrue();
        Transaction transaction = transactions.iterator().next();

        OperationResultContext<com.tradebot.core.account.transaction.Transaction<String, Long>> transactionFound = bitmexTransactionDataProviderServiceSpy
            .getTransaction(transaction.getTransactID(), transaction.getAccount().longValue());

        assertThat(transactionFound.getData().getAccountId()).isEqualTo(transaction.getAccount().longValue());
        assertThat(transactionFound.getData().getPrice()).isCloseTo(transaction.getAmount().doubleValue(), Offset.offset(0.000001));
        assertThat(transactionFound.getData().getTransactionId()).isEqualTo(transaction.getTransactID());
    }

    @Test
    public void testGetTransactionGreaterThanId() {
        assertThat(transactions.iterator().hasNext()).isTrue();
        Transaction transaction = transactions.iterator().next();

        OperationResultContext<List<com.tradebot.core.account.transaction.Transaction<String, Long>>> transactionsFound =
            bitmexTransactionDataProviderServiceSpy.getTransactionsGreaterThanId(
                "", transaction.getAccount().longValue());
        assertThat(transactionsFound.getData()).hasSize(2);

    }

}
