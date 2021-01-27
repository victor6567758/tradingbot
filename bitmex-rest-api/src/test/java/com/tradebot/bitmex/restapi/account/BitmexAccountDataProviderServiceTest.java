package com.tradebot.bitmex.restapi.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.api.UserApi;
import com.tradebot.bitmex.restapi.generated.model.Margin;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.model.Wallet;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.account.Account;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;


public class BitmexAccountDataProviderServiceTest {

    private final JSON json = new JSON();
    private final UserApi userApi = mock(UserApi.class);
    private final PositionApi positionApi = mock(PositionApi.class);

    private BitmexAccountDataProviderService bitmexAccountDataProviderServiceSpy;

    private Wallet wallet;
    private Margin margin;

    @Before
    public void init() throws ApiException, IOException {
        bitmexAccountDataProviderServiceSpy = spy(new BitmexAccountDataProviderService());

        wallet = json.deserialize(Resources.toString(Resources.getResource("walletReply.json"), StandardCharsets.UTF_8),
            new TypeToken<Wallet>() {}.getType());
        margin = json.deserialize(Resources.toString(Resources.getResource("marginReply.json"), StandardCharsets.UTF_8),
            new TypeToken<Margin>() {}.getType());

        when(userApi.userGetWallet(any(String.class))).thenReturn(wallet);
        when(userApi.userGetMargin(any(String.class))).thenReturn(margin);
        when(positionApi.positionGet(isNull(), isNull(), isNull())).thenReturn(Collections.emptyList());

        doReturn(userApi).when(bitmexAccountDataProviderServiceSpy).getUserApi();
        doReturn(positionApi).when(bitmexAccountDataProviderServiceSpy).getPositionApi();

    }

    @Test
    public void testGetLatestAccountInfo() {
        Account<Long> account = bitmexAccountDataProviderServiceSpy.getLatestAccountInfo(wallet.getAccount().longValue());

        assertThat(account.getAccountId()).isEqualTo(wallet.getAccount().longValue());
        assertThat(account.getCurrency()).isEqualTo(wallet.getCurrency());
    }

    @Test
    public void testGetLatestAccountsInfo() {
        Collection<Account<Long>> accounts = bitmexAccountDataProviderServiceSpy.getLatestAccountsInfo();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.iterator().next().getAccountId()).isEqualTo(wallet.getAccount().longValue());
        assertThat(accounts.iterator().next().getCurrency()).isEqualTo(wallet.getCurrency());
    }


}
