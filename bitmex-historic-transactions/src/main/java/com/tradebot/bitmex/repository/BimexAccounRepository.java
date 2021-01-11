package com.tradebot.bitmex.repository;

import com.tradebot.bitmex.model.BitmexAccount;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BimexAccounRepository extends JpaRepository<BitmexAccount, Long> {

    Optional<BitmexAccount> findByAccountId(@NotNull Long accountId);
}
