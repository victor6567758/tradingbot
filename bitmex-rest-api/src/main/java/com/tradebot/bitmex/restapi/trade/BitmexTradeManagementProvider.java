package com.tradebot.bitmex.restapi.trade;

import static com.tradebot.bitmex.restapi.BitmexJsonKeys.id;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.instrument;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.price;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.side;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.stopLoss;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.takeProfit;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.time;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.trades;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.units;

import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.trade.Trade;
import com.tradebot.core.trade.TradeManagementProvider;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class BitmexTradeManagementProvider implements TradeManagementProvider<Long, String, Long> {


    private static final String tradesResource = "/trades";

    private final String url;
    private final BasicHeader authHeader;

    public BitmexTradeManagementProvider(String url, String accessToken) {
        this.url = url;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    String getTradesInfoUrl(Long accountId) {
        return this.url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + tradesResource;
    }

    private Trade<Long, String, Long> parseTrade(JSONObject trade, Long accountId) {
        final Long tradeTime = Long.parseLong(trade.get(time).toString());
        final Long tradeId = (Long) trade.get(id);
        final Long tradeUnits = (Long) trade.get(units);
        final TradingSignal tradeSignal = BitmexUtils.toTradingSignal((String) trade.get(side));
        final TradeableInstrument<String> tradeInstrument = new TradeableInstrument<String>(
            (String) trade
                .get(instrument));
        final double tradeTakeProfit = ((Number) trade.get(takeProfit)).doubleValue();
        final double tradeExecutionPrice = ((Number) trade.get(price)).doubleValue();
        final double tradeStopLoss = ((Number) trade.get(stopLoss)).doubleValue();

        return new Trade<>(tradeId, tradeUnits, tradeSignal, tradeInstrument, new DateTime(
            TradingUtils.toMillisFromNanos(tradeTime)), tradeTakeProfit, tradeExecutionPrice,
            tradeStopLoss,
            accountId);

    }

    @Override
    public Collection<Trade<Long, String, Long>> getTradesForAccount(Long accountId) {
        Collection<Trade<Long, String, Long>> allTrades = Lists.newArrayList();
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(getTradesInfoUrl(accountId));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);
            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (!StringUtils.isEmpty(strResp)) {
                Object obj = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) obj;
                JSONArray accountTrades = (JSONArray) jsonResp.get(trades);
                for (Object accountTrade : accountTrades) {
                    JSONObject trade = (JSONObject) accountTrade;
                    Trade<Long, String, Long> tradeInfo = parseTrade(trade, accountId);
                    allTrades.add(tradeInfo);
                }
            } else {
                log.error("Error", resp);
            }
        } catch (Exception ex) {
            log.error("error encountered whilst fetching trades for account:" + accountId, ex);
        }
        return allTrades;
    }

    String getTradeForAccountUrl(Long tradeId, Long accountId) {
        return this.url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + tradesResource
            + TradingConstants.FWD_SLASH + tradeId;
    }

    @Override
    public Trade<Long, String, Long> getTradeForAccount(Long tradeId, Long accountId) {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(getTradeForAccountUrl(tradeId, accountId));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);
            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (strResp != StringUtils.EMPTY) {
                JSONObject trade = (JSONObject) JSONValue.parse(strResp);
                return parseTrade(trade, accountId);
            } else {
                log.error("Response error", resp);
            }
        } catch (Exception ex) {
            log.error(String
                .format("error encountered whilst fetching trade %d for account %d", tradeId,
                    accountId), ex);
        }

        return null;
    }

    HttpPatch createPatchCommand(Long accountId, Long tradeId, double stopLoss, double takeProfit)
        throws Exception {
        HttpPatch httpPatch = new HttpPatch(getTradeForAccountUrl(tradeId, accountId));
        httpPatch.setHeader(this.authHeader);
        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair(BitmexJsonKeys.takeProfit, String.valueOf(takeProfit)));
        params.add(new BasicNameValuePair(BitmexJsonKeys.stopLoss, String.valueOf(stopLoss)));
        httpPatch.setEntity(new UrlEncodedFormEntity(params));
        return httpPatch;
    }

    @Override
    public boolean modifyTrade(Long accountId, Long tradeId, double stopLoss, double takeProfit) {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPatch httpPatch = createPatchCommand(accountId, tradeId, stopLoss, takeProfit);
            log.info(TradingUtils.executingRequestMsg(httpPatch));
            HttpResponse resp = httpClient.execute(httpPatch);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (resp.getEntity() != null) {
                    log.info("Trade Modified->" + TradingUtils.responseToString(resp));
                } else {
                    log.warn(String
                        .format("trade %d could not be modified with stop loss %3.5f", tradeId,
                            stopLoss));
                }

                return true;
            } else {
                log.warn(String.format(
                    "trade %d could not be modified with stop loss %3.5f and take profit %3.5f. http code=%d",
                    tradeId, stopLoss, takeProfit, resp.getStatusLine().getStatusCode()));
            }
        } catch (Exception e) {
            log.error(
                String.format(
                    "error while modifying trade %d to stop loss %3.5f, take profit %3.5f for account %d",
                    tradeId, stopLoss, takeProfit, accountId),
                e);
        }
        return false;
    }

    @Override
    public boolean closeTrade(Long tradeId, Long accountId) {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpDelete httpDelete = new HttpDelete(getTradeForAccountUrl(tradeId, accountId));
            httpDelete.setHeader(authHeader);
            log.info(TradingUtils.executingRequestMsg(httpDelete));
            HttpResponse resp = httpClient.execute(httpDelete);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info(String
                    .format("Trade %d successfully closed for account %d", tradeId, accountId));
                return true;
            } else {
                log.warn(
                    String.format("Trade %d could not be closed. Recd error code %d", tradeId, resp
                        .getStatusLine().getStatusCode()));
            }
        } catch (Exception e) {
            log.warn("error deleting trade id:" + tradeId, e);
        }
        return false;
    }

}
