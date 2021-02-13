package com.tradebot.bitmex.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.config.TestDatasourceConfig;
import com.tradebot.bitmex.model.BitmexTransaction;
import com.tradebot.bitmex.repository.BimexAccounRepository;
import com.tradebot.bitmex.repository.BitmexTransactionRepository;
import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.bitmex.restapi.events.BitmexTransactionTypeEvent;
import com.tradebot.bitmex.restapi.generated.model.Transaction;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    TestDatasourceConfig.class,
    BitmexTransactionService.class
})
@EnableJpaRepositories(basePackageClasses = BimexAccounRepository.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class BitmexTransactionServiceTest {

    private final JSON json = new JSON();

    private AccountDataProvider<Long> accountDataProviderMock;

    private TransactionDataProvider<String, Long> transactionDataProviderMock;

    @Autowired
    private BimexAccounRepository bimexAccounRepository;

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private BitmexTransactionRepository bitmexTransactionRepository;

    @SpyBean
    private BitmexTransactionService bitmexTransactionService;

    @Before
    public void setup() throws IOException {
        List<Transaction> transactions = json.deserialize(Resources.toString(Resources.getResource("transactionsReply.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Transaction>>() {
            }.getType());

        accountDataProviderMock = mock(BitmexAccountDataProviderService.class);
        doReturn(createAccount1()).when(accountDataProviderMock).getLatestAccountsInfo();

        transactionDataProviderMock = mock(BitmexTransactionDataProviderService.class);
        List<com.tradebot.core.account.transaction.Transaction<String, Long>> newTransactions =
            transactions.stream()
                .map(this::mapToTransaction)
                .collect(Collectors.toList());

        doReturn(newTransactions).when(transactionDataProviderMock)
            .getTransactionsGreaterThanDateTime(isNull(), any(Long.class));

        doReturn(accountDataProviderMock).when(bitmexTransactionService).getBitmexAccountDataProvider();
        doReturn(transactionDataProviderMock).when(bitmexTransactionService).getBitmexTransactiondataProvider();

    }

    @Test
    public void saveNewTransactionsExistingAccountTest() {
        bitmexTransactionService.saveNewTransactions();

        assertThat(bitmexTransactionRepository.findAll().stream().map(BitmexTransaction::getTransactionId)
            .collect(Collectors.toList())).containsExactlyInAnyOrder("00000000-0000-0000-0000-000000000000",
            "ec5d009a-f504-244b-5ca4-a3681c863ec5");
    }

    @Test
    public void saveNewTransactionsNewAccountTest() {
        accountDataProviderMock = mock(BitmexAccountDataProviderService.class);
        doReturn(createAccount2()).when(accountDataProviderMock).getLatestAccountsInfo();
        doReturn(accountDataProviderMock).when(bitmexTransactionService).getBitmexAccountDataProvider();

        bitmexTransactionService.saveNewTransactions();

        assertThat(bimexAccounRepository.findByAccountId(1111L).orElse(null).getAccountId()).isEqualTo(1111L);

        assertThat(bitmexTransactionRepository.findAll().stream().map(BitmexTransaction::getTransactionId)
            .collect(Collectors.toList())).containsExactlyInAnyOrder("00000000-0000-0000-0000-000000000000",
            "ec5d009a-f504-244b-5ca4-a3681c863ec5");
    }

    @SuppressWarnings("unchecked")
    private List<Account<Long>> createAccount1() {
        Account<Long> account = mock(Account.class);
        when(account.getAccountId()).thenReturn(1001L);
        return Collections.singletonList(account);
    }

    @SuppressWarnings("unchecked")
    private List<Account<Long>> createAccount2() {
        Account<Long> account = mock(Account.class);
        when(account.getAccountId()).thenReturn(1111L);
        when(account.getCurrency()).thenReturn("XBT");

        return Collections.singletonList(account);
    }

    private com.tradebot.core.account.transaction.Transaction<String, Long> mapToTransaction(Transaction transaction) {
        return new com.tradebot.core.account.transaction.Transaction<>(
            transaction.getTransactID(),
            BitmexUtils.findByStringMarker(BitmexTransactionTypeEvent.values(),
                bitmexTransactionTypeEvent -> transaction.getTransactType().equals(bitmexTransactionTypeEvent.label())),
            transaction.getAccount().longValue(),
            StringUtils.isNotBlank(transaction.getAddress()) ?
                instrumentService.resolveTradeableInstrumentNoException(transaction.getAddress()) : null,
            transaction.getAmount().longValue(),
            TradingSignal.NONE,
            transaction.getTransactTime(),
            transaction.getAmount().doubleValue(),
            0.0,
            0.0);
    }
}