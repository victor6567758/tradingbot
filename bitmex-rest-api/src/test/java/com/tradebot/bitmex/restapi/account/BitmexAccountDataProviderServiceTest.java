package com.tradebot.bitmex.restapi.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Wallet;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.account.Account;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;


public class BitmexAccountDataProviderServiceTest {

    private final JSON json = new JSON();
    private final UserApi userApi = mock(UserApi.class);
    private final BitmexAccountDataProviderService bitmexAccountDataProviderService = new BitmexAccountDataProviderService();

    private Wallet wallet;

    @Before
    public void init() throws ApiException, IOException {
        BitmexAccountDataProviderService bitmexAccountDataProviderServiceSpy = spy(bitmexAccountDataProviderService);

        wallet = json.deserialize(Resources.toString(Resources.getResource("walletReply.json"), StandardCharsets.UTF_8),
            new TypeToken<Wallet>() {}.getType());

        when(userApi.userGetWallet(eq(wallet.getCurrency()))).thenReturn(wallet);
        doReturn(userApi).when(bitmexAccountDataProviderServiceSpy).getUserApi();

    }

    @Test
    public void testGetLatestAccountInfo() {
        Account<Long> account = bitmexAccountDataProviderService.getLatestAccountInfo(wallet.getAccount().longValue());

        assertThat(account.getAccountId()).isEqualTo(wallet.getAccount().longValue());
        assertThat(account.getCurrency()).isEqualTo(wallet.getCurrency());
    }

    @Test
    public void getLatestAccountsInfo() {
        Collection<Account<Long>> accounts = bitmexAccountDataProviderService.getLatestAccountsInfo();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.iterator().next().getAccountId()).isEqualTo(wallet.getAccount().longValue());
        assertThat(accounts.iterator().next().getCurrency()).isEqualTo(wallet.getCurrency());
    }


}
