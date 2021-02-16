package com.tradebot.bitmex.restapi.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.withinPercentage;

import com.tradebot.core.instrument.TradeableInstrument;
import java.math.BigDecimal;
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