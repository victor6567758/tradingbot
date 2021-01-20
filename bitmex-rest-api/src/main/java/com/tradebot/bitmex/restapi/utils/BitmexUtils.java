package com.tradebot.bitmex.restapi.utils;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.InputStream;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

@UtilityClass
public class BitmexUtils {

    private static final String BITMEX_ACCOUNT_YML = "bitmex-account.yml";

    public static BitmexAccountConfiguration readBitmexCredentials() {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);

        Yaml yaml = new Yaml(new Constructor(BitmexAccountConfiguration.class), representer);
        InputStream inputStream = BitmexAccountConfiguration.class.getClassLoader()
            .getResourceAsStream(BITMEX_ACCOUNT_YML);
        return yaml.load(inputStream);
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


}
