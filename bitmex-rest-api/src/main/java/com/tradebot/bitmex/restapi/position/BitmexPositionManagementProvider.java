package com.tradebot.bitmex.restapi.position;

import static com.tradebot.bitmex.restapi.BitmexJsonKeys.avgPrice;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.instrument;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.positions;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.side;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.units;

import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.position.Position;
import com.tradebot.core.position.PositionManagementProvider;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


@Slf4j
public class BitmexPositionManagementProvider implements PositionManagementProvider<String, Long> {


    private final String url;
    private final BasicHeader authHeader;
    private static final String positionsResource = "/positions";

    public BitmexPositionManagementProvider(String url, String accessToken) {
        this.url = url;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    private Position<String> parsePositionInfo(JSONObject accPosition) {
        Position<String> positionInfo = new Position<String>(
            new TradeableInstrument<String>((String) accPosition
                .get(instrument)), (Long) accPosition.get(units),
            BitmexUtils.toTradingSignal((String) accPosition
                .get(side)), (Double) accPosition.get(avgPrice));
        return positionInfo;
    }

    String getPositionsForAccountUrl(Long accountId) {
        return this.url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + positionsResource;
    }

    @Override
    public Collection<Position<String>> getPositionsForAccount(Long accountId) {
        Collection<Position<String>> allPositions = Lists.newArrayList();
        try (CloseableHttpClient httpClient = getHttpClient()) {
            String strResp = getResponseAsString(getPositionsForAccountUrl(accountId), httpClient);
            if (!StringUtils.isEmpty(strResp)) {
                Object obj = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) obj;
                JSONArray accPositions = (JSONArray) jsonResp.get(positions);
                for (Object o : accPositions) {
                    JSONObject accPosition = (JSONObject) o;
                    Position<String> positionInfo = parsePositionInfo(accPosition);
                    allPositions.add(positionInfo);
                }
            }
        } catch (Exception ex) {
            log.error("error encountered whilst fetching positions for account:" + accountId, ex);
        }
        return allPositions;
    }

    private String getResponseAsString(String reqUrl, CloseableHttpClient httpClient)
        throws Exception {
        HttpUriRequest httpGet = new HttpGet(reqUrl);
        httpGet.setHeader(this.authHeader);
        log.info(TradingUtils.executingRequestMsg(httpGet));
        HttpResponse resp = httpClient.execute(httpGet);
        String strResp = TradingUtils.responseToString(resp);
        return strResp;
    }

    String getPositionForInstrumentUrl(Long accountId, TradeableInstrument<String> instrument) {
        return this.url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + positionsResource
            + TradingConstants.FWD_SLASH + instrument.getInstrument();
    }

    @Override
    public boolean closePosition(Long accountId, TradeableInstrument<String> instrument) {

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpDelete httpDelete = new HttpDelete(
                getPositionForInstrumentUrl(accountId, instrument));
            httpDelete.setHeader(authHeader);
            log.info(TradingUtils.executingRequestMsg(httpDelete));
            HttpResponse resp = httpClient.execute(httpDelete);
            int httpCode = resp.getStatusLine().getStatusCode();
            if (httpCode == HttpStatus.SC_OK) {
                log.info(String
                    .format("Position successfully closed for instrument %s and account %d",
                        instrument
                            .getInstrument(), accountId));
                return true;
            } else {
                log.warn(String.format(
                    "Position for instrument %s and account %d not closed. Encountered error code=%d",
                    instrument
                        .getInstrument(), accountId, httpCode));
            }
        } catch (Exception ex) {
            log.error(String.format(
                "error encountered whilst closing position for instrument %s and account %d",
                instrument.getInstrument(), accountId), ex);
        }
        return false;
    }

    @Override
    public Position<String> getPositionForInstrument(Long accountId,
        TradeableInstrument<String> instrument) {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            String strResp = getResponseAsString(getPositionForInstrumentUrl(accountId, instrument),
                httpClient);
            if (strResp != StringUtils.EMPTY) {
                return parsePositionInfo((JSONObject) JSONValue.parse(strResp));
            }
        } catch (Exception ex) {
            log.error(String.format(
                "error encountered whilst fetching position for instrument %s and account %d",
                instrument.getInstrument(), accountId), ex);
        }
        return null;
    }
}
