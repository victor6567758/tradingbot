
package com.tradebot.core.account;

import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;

public interface AccountDataProvider<K> {

    OperationResultContext<Account<K>> getLatestAccountInfo(K accountId);

    OperationResultContext<Collection<Account<K>>> getLatestAccountsInfo();
}
