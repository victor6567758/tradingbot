package com.tradebot.bitmex.restapi.marketdata;

import static com.tradebot.bitmex.restapi.BitmexJsonKeys.ask;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.bid;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.time;

import com.google.common.collect.Maps;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


@Slf4j
public class BitmexCurrentPriceInfoProvider implements CurrentPriceInfoProvider<String> {


    private final String url;
    private final BasicHeader authHeader;

    public BitmexCurrentPriceInfoProvider(String url, String accessToken) {
        this.url = url;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    @Override
    public Map<TradeableInstrument<String>, Price<String>> getCurrentPricesForInstruments(
        Collection<TradeableInstrument<String>> instruments) {
        StringBuilder instrumentCsv = new StringBuilder();
        boolean firstTime = true;
        for (TradeableInstrument<String> instrument : instruments) {
            if (firstTime) {
                firstTime = false;
            } else {
                instrumentCsv.append(TradingConstants.ENCODED_COMMA);
            }
            instrumentCsv.append(instrument.getInstrument());
        }

        Map<TradeableInstrument<String>, Price<String>> pricesMap = Maps.newHashMap();

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(
                this.url + BitmexConstants.PRICES_RESOURCE + "?instruments="
                    + instrumentCsv.toString());
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);
            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (!StringUtils.isEmpty(strResp)) {
                Object obj = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) obj;
                JSONArray prices = (JSONArray) jsonResp.get(BitmexJsonKeys.prices);
                for (Object price : prices) {
                    JSONObject trade = (JSONObject) price;
                    long priceTime = Long.parseLong(trade.get(time).toString());
                    TradeableInstrument<String> instrument = new TradeableInstrument<>(
                        (String) trade
                            .get(BitmexJsonKeys.instrument));
                    Price<String> pi = new Price<>(instrument,
                        ((Number) trade.get(bid)).doubleValue(),
                        ((Number) trade.get(ask)).doubleValue(), new DateTime(TradingUtils
                        .toMillisFromNanos(priceTime)));
                    pricesMap.put(instrument, pi);
                }
            } else {
                log.error("Error: {}", resp);
            }
        } catch (Exception ex) {
            log.error("Error", ex);
        }
        return pricesMap;
    }

    @Override
    public Price<String> getCurrentPricesForInstrument(TradeableInstrument<String> instrument) {
        throw new IllegalArgumentException("Not implemented");
    }

}
