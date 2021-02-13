package com.tradebot.bitmex.restapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.withinPercentage;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.bitmex.restapi.model.OrderStatus;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.TradeableInstrument;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


public class BitmexUtilsTest {

    private static final TradeableInstrument INSTRUMENT =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);

    @Test
    public void testRoundPriceOk() {
        double result = BitmexUtils.roundPrice(INSTRUMENT, 48299.5);
        assertThat(result).isCloseTo(48299.5, withinPercentage(0.001));
    }

    @Test
    public void testRoundPriceLowBound() {
        double result = BitmexUtils.roundPrice(INSTRUMENT, 48299.6);
        assertThat(result).isCloseTo(48299.5, withinPercentage(0.001));
    }

    @Test
    public void testRoundPriceHiBound() {
        double result = BitmexUtils.roundPrice(INSTRUMENT, 48300.1);
        assertThat(result).isCloseTo(48300, withinPercentage(0.001));
    }
}