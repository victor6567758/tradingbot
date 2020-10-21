package com.tradebot.bitmex.restapi.instrument;

import static com.tradebot.bitmex.restapi.BitmexJsonKeys.ask;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.bid;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.instruments;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.interestRate;

import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.InstrumentPairInterestRate;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
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


@Slf4j
public class BitmexInstrumentDataProviderService implements InstrumentDataProvider<String> {


    private final String url;
    private final long accountId;
    private final BasicHeader authHeader;
    static final String fieldsRequested = "instrument%2Cpip%2CinterestRate";

    public BitmexInstrumentDataProviderService(String url, long accountId, String accessToken) {
        this.url = url;
        this.accountId = accountId;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    String getInstrumentsUrl() {
        return url + BitmexConstants.INSTRUMENTS_RESOURCE + "?accountId=" + accountId + "&fields="
            + fieldsRequested;
    }

    @Override
    public Collection<TradeableInstrument<String>> getInstruments() {
        Collection<TradeableInstrument<String>> instrumentsList = Lists.newArrayList();
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(getInstrumentsUrl());
            httpGet.setHeader(authHeader);
            log.info(TradingUtils.executingRequestMsg(httpGet));

            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (!StringUtils.isEmpty(strResp)) {
                Object obj = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) obj;
                JSONArray instrumentArray = (JSONArray) jsonResp.get(instruments);

                for (Object o : instrumentArray) {
                    JSONObject instrumentJson = (JSONObject) o;
                    String instrument = (String) instrumentJson.get(BitmexJsonKeys.instrument);
                    String[] currencies = BitmexUtils.splitCcyPair(instrument);
                    double pip = Double
                        .parseDouble(instrumentJson.get(BitmexJsonKeys.pip).toString());
                    JSONObject interestRates = (JSONObject) instrumentJson.get(interestRate);
                    if (interestRates.size() != 2) {
                        throw new IllegalArgumentException();
                    }

                    JSONObject currency1Json = (JSONObject) interestRates.get(currencies[0]);
                    JSONObject currency2Json = (JSONObject) interestRates.get(currencies[1]);

                    final double baseCurrencyBidInterestRate = ((Number) currency1Json.get(bid))
                        .doubleValue();
                    final double baseCurrencyAskInterestRate = ((Number) currency1Json.get(ask))
                        .doubleValue();
                    final double quoteCurrencyBidInterestRate = ((Number) currency2Json.get(bid))
                        .doubleValue();
                    final double quoteCurrencyAskInterestRate = ((Number) currency2Json.get(ask))
                        .doubleValue();

                    InstrumentPairInterestRate instrumentPairInterestRate = new InstrumentPairInterestRate(
                        baseCurrencyBidInterestRate, baseCurrencyAskInterestRate,
                        quoteCurrencyBidInterestRate,
                        quoteCurrencyAskInterestRate);
                    TradeableInstrument<String> tradeableInstrument = new TradeableInstrument<>(
                        instrument, pip,
                        instrumentPairInterestRate, null);
                    instrumentsList.add(tradeableInstrument);
                }
            } else {
                log.error("Error message: {}", resp);
            }
        } catch (Exception e) {
            log.error("exception encountered whilst retrieving all instruments info", e);
        }
        return instrumentsList;
    }

}
