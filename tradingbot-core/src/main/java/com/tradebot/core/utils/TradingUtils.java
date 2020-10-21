
package com.tradebot.core.utils;

import com.tradebot.core.TradingSignal;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

@Slf4j
public class TradingUtils {

    private TradingUtils() {
    }

    public static final int CCY_PAIR_LEN = 7;
    public static final int CCY_SEP_CHR_POS = 3;
    private static final int THOUSAND = 1000;

    public static String executingRequestMsg(HttpRequest request) {
        return "Executing request : " + request.getRequestLine();
    }

    public static long toUnixTime(DateTime dateTime) {
        return dateTime.getMillis() * THOUSAND;
    }

    public static long toMillisFromNanos(long nanos) {
        return nanos / THOUSAND;
    }

    public static String[] splitCcyPair(String instrument, String currencySeparator) {
        return StringUtils.split(instrument, currencySeparator);
    }

    public static double calculateTakeProfitPrice(double tickSize, TradingSignal signal,
        double bidPrice,
        double askPrice, int pipsDesired) {
        switch (signal) {
            case LONG:
                return askPrice + tickSize * pipsDesired;
            case SHORT:
                return bidPrice - tickSize * pipsDesired;
            default:
                return 0.0;
        }
    }

    public static String responseToString(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if ((response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
            || response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
            && entity != null) {

            InputStream stream = entity.getContent();
            String line;
            try (Reader reader = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(reader)
            ) {
                StringBuilder strResp = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    strResp.append(line);
                }

                return strResp.toString();
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

    public static String[] splitInstrumentPair(String instrument) {
        if (!StringUtils.isEmpty(instrument) && instrument.length() == CCY_PAIR_LEN) {
            return new String[]{instrument.substring(0, CCY_SEP_CHR_POS),
                instrument.substring(CCY_SEP_CHR_POS + 1)};
        }
        throw new IllegalArgumentException(
            String.format("Instrument %s is not of expected length %d", instrument, CCY_PAIR_LEN));
    }

    public static int getSign(String currencyInstrument, TradingSignal side, String currency) {
        int sign = 0;
        if (currencyInstrument.contains(currency)) {
            String[] ccyPairs = splitInstrumentPair(currencyInstrument);
            if ((currency.equals(ccyPairs[0]) && side == TradingSignal.LONG)
                || (currency.equals(ccyPairs[1]) && side == TradingSignal.SHORT)) {
                return 1;
            } else {
                return -1;
            }
        }
        return sign;
    }

    public static String getResponse(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
}
