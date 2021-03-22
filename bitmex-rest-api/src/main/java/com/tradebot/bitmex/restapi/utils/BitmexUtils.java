package com.tradebot.bitmex.restapi.utils;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.utils.TradingConstants;
import com.tradebot.core.utils.TradingUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

@UtilityClass
@Slf4j
public class BitmexUtils {

    private static final String BITMEX_DEFAULT_ACCOUNT_YML = "bitmex-account.yml";
    private static final String ENV_CONFIG_YML_PATH = "PROD_CONF_YML";

    public static BitmexAccountConfiguration readBitmexConfiguration() {

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(BitmexAccountConfiguration.class), representer);

        String configYml = System.getProperty(ENV_CONFIG_YML_PATH);
        if (StringUtils.isNotBlank(configYml)) {
            try (InputStream inputStream = FileUtils.openInputStream(FileUtils.getFile(configYml))) {
                log.debug("Read configuration from {}", configYml);
                return yaml.load(inputStream);
            } catch (IOException e) {
                log.warn("External configuration file is not readable {}", configYml);
            }
        }

        try (InputStream inputStream = BitmexAccountConfiguration.class.getClassLoader().getResourceAsStream(BITMEX_DEFAULT_ACCOUNT_YML)) {
            log.debug("Read configuration from {}", BITMEX_DEFAULT_ACCOUNT_YML);
            return yaml.load(inputStream);
        } catch (IOException e) {
            log.warn("Classpath configuration file is not readable {}", BITMEX_DEFAULT_ACCOUNT_YML);
        }
        throw new IllegalArgumentException("Neither of configuration yaml files could be read");
    }


    public static <T> T findByStringMarker(T[] list, Predicate<T> predicate) {
        for (T element : list) {
            if (predicate.test(element)) {
                return element;
            }
        }
        throw new IllegalArgumentException("Cannot find a value");
    }

    public static String getSymbol(TradeableInstrument instrument) {
        return StringUtils.substring(instrument.getInstrument(), 0, 3);
    }


    public static String isoCcyToExchangeCcy(String ccy) {
        int expectedLen = 6;
        if (!StringUtils.isEmpty(ccy) && ccy.length() == expectedLen) {
            return String.format("%s%s%s", ccy.substring(0, 3), BitmexConstants.CCY_PAIR_SEP,
                ccy.substring(3));
        }
        throw new IllegalArgumentException(
            String.format("expected a string with length = %d but got %s", expectedLen,
                ccy));
    }

    public static double roundPrice(TradeableInstrument instrument, double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Invalid price");
        }

        double tickSize = instrument.getTickSize();
        double curPrice = price;
        int scale = instrument.getScale();

        while (scale > 0) {
            tickSize *= 10;
            curPrice *= 10;

            scale--;
        }

        long longPrice = (long) curPrice;
        long longTickSize = (long) tickSize;

        double resultPrice = closestDividableNumber(longPrice, longTickSize);
        while (scale < instrument.getScale()) {
            resultPrice /= 10;
            scale++;
        }

        return resultPrice;
    }

    public static long closestDividableNumber(long n, long m) {
        long q = n / m;
        long n1 = m * q;
        long n2 = (n * m) > 0 ? (m * (q + 1)) : (m * (q - 1));

        if (Math.abs(n - n1) < Math.abs(n - n2)) {
            return n1;
        }

        return n2;
    }

    public static String errorMessageFromApiException(ApiException apiException) {
        return String.format("Code [%d], message [%s], headers [%s], body [%s]",
            apiException.getCode(), apiException.getMessage(),
            apiException.getResponseHeaders().entrySet().stream()
                .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
                .map(entry -> entry.getKey() + ":" + entry.getValue().get(0))
                .collect(Collectors.joining(", ")),
            apiException.getResponseBody());
    }

    public static <N, R> void prepareResult(ApiResponse<N> apiResponse, BitmexOperationQuotas<R> result) {
        result.setXRatelimitLimit(getIntHeaderValue("x-ratelimit-limit", apiResponse.getHeaders()));
        result.setXRatelimitRemaining(getIntHeaderValue("x-ratelimit-remaining", apiResponse.getHeaders()));
        result.setXRatelimitReset(getIntHeaderValue("x-ratelimit-reset", apiResponse.getHeaders()) * 1000L);
        result.setXRatelimitRemaining1s(getIntHeaderValue("x-ratelimit-remaining-1s", apiResponse.getHeaders()));
    }

    public static <N, R> BitmexOperationQuotas<R> prepareResultReturned(ApiResponse<N> apiResponse, BitmexOperationQuotas<R> result) {
        result.setXRatelimitLimit(getIntHeaderValue("x-ratelimit-limit", apiResponse.getHeaders()));
        result.setXRatelimitRemaining(getIntHeaderValue("x-ratelimit-remaining", apiResponse.getHeaders()));
        result.setXRatelimitReset(getIntHeaderValue("x-ratelimit-reset", apiResponse.getHeaders()) * 1000L);
        result.setXRatelimitRemaining1s(getIntHeaderValue("x-ratelimit-remaining-1s", apiResponse.getHeaders()));

        return result;
    }

    public static String fromIsoFormat(String instrument) {
        return BitmexUtils.isoCcyToExchangeCcy(instrument);
    }

    public static String fromPairSeparatorFormat(String instrument) {
        String[] pair = TradingUtils.splitInstrumentPair(instrument);
        return String.format("%s%s%s", pair[0], BitmexConstants.CCY_PAIR_SEP, pair[1]);
    }

    public static String toIsoFormat(String instrument) {
        String[] tokens = TradingUtils.splitCcyPair(instrument, TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
        return tokens[0] + tokens[1];
    }

    public static int getIntHeaderValue(String name, Map<String, List<String>> headers) {
        return NumberUtils.toInt(headers.getOrDefault(name, Collections.singletonList("-1")).get(0), -1);
    }

}
