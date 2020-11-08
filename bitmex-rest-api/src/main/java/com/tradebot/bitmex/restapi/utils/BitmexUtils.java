package com.tradebot.bitmex.restapi.utils;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.events.Event;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.OrderType;
import com.tradebot.core.utils.TradingUtils;
import java.io.InputStream;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicHeader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@UtilityClass
public class BitmexUtils {

    public static BitmexAccountConfiguration readBitmexCredentials() {
        Yaml yaml = new Yaml(new Constructor(BitmexAccountConfiguration.class));
        InputStream inputStream = BitmexAccountConfiguration.class.getClassLoader()
            .getResourceAsStream("bitmex-account.yml");
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

    public static String getSymbol(TradeableInstrument<String> instrument) {
        return StringUtils.substring(instrument.getInstrument(), 0, 3);
    }

    // -----------------


    public static BasicHeader createAuthHeader(String accessToken) {
        return new BasicHeader("Authorization", "Bearer " + accessToken);
    }

    public static String[] splitCcyPair(String instrument) {
        return TradingUtils.splitCcyPair(instrument, BitmexConstants.CCY_PAIR_SEP);
    }

    public static String toBitmexCcy(String baseCcy, String quoteCcy) {
        final int expectedLen = 3;
        if (!StringUtils.isEmpty(baseCcy) && !StringUtils.isEmpty(quoteCcy)
            && baseCcy.length() == expectedLen
            && quoteCcy.length() == expectedLen) {
            return String.format("%s%s%s", baseCcy, BitmexConstants.CCY_PAIR_SEP, quoteCcy);
        }
        throw new IllegalArgumentException(
            String.format("base currency and quote currency cannot be null or empty"
                + " and must be %d char length", expectedLen));
    }

    public static String isoCcyToOandaCcy(String ccy) {
        int expectedLen = 6;
        if (!StringUtils.isEmpty(ccy) && ccy.length() == expectedLen) {
            return String.format("%s%s%s", ccy.substring(0, 3), BitmexConstants.CCY_PAIR_SEP,
                ccy.substring(3));
        }
        throw new IllegalArgumentException(
            String.format("expected a string with length = %d but got %s", expectedLen,
                ccy));
    }

    public static String bitmexToHashTagCcy(String oandaCcy) {
        String[] currencies = BitmexUtils.splitCcyPair(oandaCcy);
        String instrumentAsHashtag = TradingConstants.HASHTAG + currencies[0] + currencies[1];
        return instrumentAsHashtag;
    }

    public static String hashTagCcyToOandaCcy(String ccy) {
        final int expectedLen = TradingUtils.CCY_PAIR_LEN;
        if (!StringUtils.isEmpty(ccy) && ccy.startsWith(TradingConstants.HASHTAG)
            && ccy.length() == expectedLen) {

            return isoCcyToOandaCcy(ccy.substring(1));
        }
        throw new IllegalArgumentException(String.format(
            "expected a string with length = %d beginning with %s but got %s", expectedLen,
            TradingConstants.HASHTAG, ccy));
    }

    public static TradingSignal toTradingSignal(String side) {
        if (BitmexConstants.BUY.equals(side)) {
            return TradingSignal.LONG;
        } else if (BitmexConstants.SELL.equals(side)) {
            return TradingSignal.SHORT;
        } else {
            return TradingSignal.NONE;
        }
    }

    public static OrderType toOrderType(String type) {
        if (BitmexConstants.ORDER_MARKET.equals(type)) {
            return OrderType.MARKET;
        } else if (BitmexConstants.ORDER_LIMIT.equals(type)
            || BitmexConstants.ORDER_MARKET_IF_TOUCHED.equals(type)) {
            return OrderType.LIMIT;
        } else {
            throw new IllegalArgumentException("Unsupported order type:" + type);
        }
    }

    public static String toType(OrderType orderType) {
        switch (orderType) {
            case LIMIT:
                return BitmexConstants.ORDER_LIMIT;
            case MARKET:
                return BitmexConstants.ORDER_MARKET;
            default:
                return null;
        }
    }

    public static String toSide(TradingSignal signal) {
        switch (signal) {
            case LONG:
                return BitmexConstants.BUY;
            case SHORT:
                return BitmexConstants.SELL;
            default:
                return BitmexConstants.NONE;
        }
    }

    private static Event findAppropriateType(Event[] events, String transactionType) {
        for (Event evt : events) {
            if (evt.name().equals(transactionType)) {
                return evt;
            }
        }
        return null;
    }

}
