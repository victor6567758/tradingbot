package com.tradebot.bitmex.service;

import com.tradebot.bitmex.model.BitmexAccount;
import com.tradebot.bitmex.model.BitmexTransaction;
import com.tradebot.bitmex.repository.BimexAccounRepository;
import com.tradebot.bitmex.repository.BitmexTransactionRepository;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.account.transaction.Transaction;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.model.OperationResultContext;
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

    private final AccountDataProvider<Long> accountDataProvider;

    private final BitmexTransactionRepository bitmexTransactionRepository;

    private final BimexAccounRepository bitmexAccountRepository;

    @Getter(AccessLevel.PACKAGE)
    private final TransactionDataProvider<String, Long> bitmexTransactiondataProvider;

    public void saveNewTransactions() {
        OperationResultContext<Collection<Account<Long>>> allAccountsWithContext = accountDataProvider.getLatestAccountsInfo();
        if (!allAccountsWithContext.isResult()) {
            throw new IllegalArgumentException("Cannot obtain account information");
        }

        Collection<Account<Long>> allAccounts = allAccountsWithContext.getData();

        for (Account<Long> account : allAccounts) {
            BitmexAccount bitmexAcc = bitmexAccountRepository.findByAccountId(account.getAccountId())
                .orElseGet(() -> bitmexAccountRepository.save(new BitmexAccount(account.getAccountId(), account.getCurrency())));

            Timestamp maxTransactionTime = bitmexTransactionRepository.getMaxTransactionTimeForAccount(bitmexAcc)
                .orElse(null);

            List<Transaction<String, Long>> newTransactions =
                getBitmexTransactiondataProvider().getTransactionsGreaterThanDateTime(
                    maxTransactionTime != null ? new DateTime(maxTransactionTime) : null, account.getAccountId());
            log.info("Found {} new transactions for account {}", newTransactions.size(), account.getAccountId());

            for (Transaction<String, Long> transaction : newTransactions) {
                log.info("Transaction type {}, price {}", transaction.getTransactionType(), transaction.getPrice());

                BitmexTransaction bitmexTransaction = fromTransaction(transaction);
                bitmexTransaction.setAccount(bitmexAcc);

                bitmexTransactionRepository.save(bitmexTransaction);
            }
        }

    }

    private BitmexTransaction fromTransaction(Transaction<String, Long> transaction) {
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
        if (transaction.getInstrument() != null) {
            bitmexTransaction.setInstrument(transaction.getInstrument().getInstrument());
        }
        return bitmexTransaction;
    }


}
