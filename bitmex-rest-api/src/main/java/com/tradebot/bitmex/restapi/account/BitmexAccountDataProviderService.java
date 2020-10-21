package com.tradebot.bitmex.restapi.account;

import static com.tradebot.bitmex.restapi.BitmexConstants.ACCOUNTS_RESOURCE;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.accountCurrency;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.accountId;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.balance;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.marginAvail;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.marginRate;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.marginUsed;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.openTrades;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.realizedPl;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.unrealizedPl;

import com.tradebot.core.TradingConstants;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.utils.TradingUtils;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;


@Slf4j
public class BitmexAccountDataProviderService implements AccountDataProvider<Long> {


    private final String url;
    private final String userName;
    private final BasicHeader authHeader;

    public BitmexAccountDataProviderService(final String url, final String userName, final String accessToken) {
        this.url = url;
        this.userName = userName;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    String getSingleAccountUrl(Long accountId) {
        return url + ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId;
    }

    String getAllAccountsUrl() {
        return this.url + ACCOUNTS_RESOURCE + "?username=" + this.userName;
    }

    private Account<Long> getLatestAccountInfo(final Long accountId, CloseableHttpClient httpClient) {
        try {
            HttpUriRequest httpGet = new HttpGet(getSingleAccountUrl(accountId));
            httpGet.setHeader(authHeader);

            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(httpResponse);
            if (!StringUtils.isEmpty(strResp)) {
                Object obj = JSONValue.parse(strResp);
                JSONObject accountJson = (JSONObject) obj;

                /*Parse JSON response for account information*/
                final double accountBalance = ((Number) accountJson.get(balance)).doubleValue();
                final double accountUnrealizedPnl = ((Number) accountJson.get(unrealizedPl)).doubleValue();
                final double accountRealizedPnl = ((Number) accountJson.get(realizedPl)).doubleValue();
                final double accountMarginUsed = ((Number) accountJson.get(marginUsed)).doubleValue();
                final double accountMarginAvailable = ((Number) accountJson.get(marginAvail)).doubleValue();
                final Long accountOpenTrades = (Long) accountJson.get(openTrades);
                final String accountBaseCurrency = (String) accountJson.get(accountCurrency);
                final Double accountLeverage = (Double) accountJson.get(marginRate);

                Account<Long> accountInfo = new Account<Long>(accountBalance, accountUnrealizedPnl, accountRealizedPnl,
                        accountMarginUsed, accountMarginAvailable, accountOpenTrades, accountBaseCurrency, accountId,
                        accountLeverage);

                return accountInfo;
            } else {
                log.error("Error: {}", httpResponse);
            }
        } catch (Exception e) {
            log.error("Exception encountered whilst getting info for account:" + accountId, e);
        }
        return null;
    }

    @Override
    public Account<Long> getLatestAccountInfo(final Long accountId) {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            return getLatestAccountInfo(accountId, httpClient);
        } catch (IOException ioException) {
            throw new IllegalArgumentException(ioException);
        }
    }

    @Override
    public Collection<Account<Long>> getLatestAccountInfo() {


        List<Account<Long>> accInfos = Lists.newArrayList();
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(getAllAccountsUrl());
            httpGet.setHeader(this.authHeader);

            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (!StringUtils.isEmpty(strResp)) {
                Object jsonObject = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) jsonObject;
                JSONArray accounts = (JSONArray) jsonResp.get(BitmexJsonKeys.accounts);
                /*
                 * We are doing a per account json request because not all information is returned in the array of results
                 */
                for (Object o : accounts) {
                    JSONObject account = (JSONObject) o;
                    Long accountIdentifier = (Long) account.get(accountId);
                    Account<Long> accountInfo = getLatestAccountInfo(accountIdentifier, httpClient);
                    accInfos.add(accountInfo);
                }
            } else {
                log.error("Error: {}", resp);
            }

        } catch (Exception e) {
            log.error("Exception encountered while retrieving all accounts data", e);
        }
        return accInfos;
    }

}
