package com.tradebot.bitmex.service;

import com.tradebot.bitmex.model.BitmexAccount;
import com.tradebot.bitmex.model.BitmexTransaction;
import com.tradebot.bitmex.repository.BimexAccounRepository;
import com.tradebot.bitmex.repository.BitmexTransactionRepository;
import com.tradebot.bitmex.restapi.account.BitmexAccountDataProviderService;
import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.Transaction;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class BitmexTransactionService {

    @Getter(AccessLevel.PACKAGE)
    private final AccountDataProvider<Long> bitmexAccountDataProvider = new BitmexAccountDataProviderService();

    private final BitmexTransactionRepository bitmexTransactionRepository;

    private final BimexAccounRepository bitmexAccountRepository;

    @Getter(AccessLevel.PACKAGE)
    private final TransactionDataProvider<String, Long, String> bitmexTransactiondataProvider =
        new BitmexTransactionDataProviderService();

    public void saveNewTransactions() {
        Collection<Account<Long>> allAccounts = getBitmexAccountDataProvider().getLatestAccountsInfo();

        for (Account<Long> account : allAccounts) {
            BitmexAccount bitmexAcc = bitmexAccountRepository.findByAccountId(account.getAccountId())
                .orElseGet(() -> bitmexAccountRepository.save(new BitmexAccount(account.getAccountId(), account.getCurrency())));

            DateTime maxTransactionTime = bitmexTransactionRepository.getMaxTransactionTimeForAccount(bitmexAcc)
                .orElse(null);

            List<Transaction<String, Long, String>> newTransactions =
                getBitmexTransactiondataProvider().getTransactionsGreaterThanDateTime(maxTransactionTime, account.getAccountId());
            log.info("Found {} new transactions for account {}", newTransactions.size(), account.getAccountId());

            for (Transaction<String, Long, String> transaction : newTransactions) {
                log.info("Transaction type {}, price {}", transaction.getTransactionType(), transaction.getPrice());

                BitmexTransaction bitmexTransaction = fromTransaction(transaction);
                bitmexTransaction.setAccount(bitmexAcc);

                bitmexTransactionRepository.save(bitmexTransaction);
            }
        }

    }

    private BitmexTransaction fromTransaction(Transaction<String, Long, String> transaction) {
        BitmexTransaction bitmexTransaction = new BitmexTransaction();

        bitmexTransaction.setInterest(transaction.getInterest());
        bitmexTransaction.setLinkedTransactionId(transaction.getLinkedTransactionId());
        bitmexTransaction.setPnl(transaction.getPnl());
        bitmexTransaction.setPrice(transaction.getPrice() == null ? 0.0 : transaction.getPrice());
        bitmexTransaction.setTransactionId(transaction.getTransactionId());
        if (transaction.getTransactionTime() != null) {
            bitmexTransaction.setTransactionTime(new Timestamp(transaction.getTransactionTime().toDate().getTime()));
        }
        bitmexTransaction.setTransactionType(transaction.getTransactionType().name());
        bitmexTransaction.setUnits(transaction.getUnits());
        bitmexTransaction.setInstrument(transaction.getInstrument().getInstrument());
        return bitmexTransaction;
    }


}
