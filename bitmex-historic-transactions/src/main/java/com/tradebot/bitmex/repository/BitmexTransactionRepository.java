package com.tradebot.bitmex.repository;

import com.tradebot.bitmex.model.BitmexAccount;
import com.tradebot.bitmex.model.BitmexTransaction;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BitmexTransactionRepository extends JpaRepository<BitmexTransaction, Long> {

    @Query(value = "select max(transactionTime) FROM BitmexTransaction where account = :bitmexAccount")
    Optional<DateTime> getMaxTransactionTimeForAccount(@NotNull @Param("bitmexAccount") BitmexAccount bitmexAccount);
}
