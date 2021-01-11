package com.tradebot.bitmex.restapi.helper;

import static org.junit.Assert.assertEquals;

import com.tradebot.core.helper.ProviderHelper;
import org.junit.Test;


public class BitmexProviderHelperTest {

    private final ProviderHelper providerHelper = new BitmexProviderHelper();

    @Test
    public void fromIsoFormatTest() {
        String currencyPair = providerHelper.fromIsoFormat("XAUUSD");
        assertEquals("XAU_USD", currencyPair);
    }

    @Test
    public void fromPairSeparatorFormatTest() {
        assertEquals("GBP_NZD", this.providerHelper.fromPairSeparatorFormat("GBP/NZD"));
    }

    @Test
    public void toIsoFormatTest() {
        assertEquals("USDJPY", this.providerHelper.toIsoFormat("USD_JPY"));
    }


}
