package com.tradebot.bitmex.restapi.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.QuoteApi;
import com.tradebot.bitmex.restapi.generated.model.Quote;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.Price;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.data.Offset;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;


public class BitmexCurrentPriceInfoProviderTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.dateTime().getPrinter(), ISODateTimeFormat.dateOptionalTimeParser().getParser())
        .toFormatter();

    private static final TradeableInstrument INSTRUMENT =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);
    private final JSON json = new JSON();
    private final QuoteApi quoteApi = mock(QuoteApi.class);

    private BitmexCurrentPriceInfoProvider bitmexCurrentPriceInfoProviderSpy;
    private List<Quote> quotes;
    private InstrumentService instrumentServiceSpy;

    @Before
    public void init() throws ApiException, IOException {

        instrumentServiceSpy = mock(InstrumentService.class);
        doReturn(INSTRUMENT).when(instrumentServiceSpy).resolveTradeableInstrument(INSTRUMENT.getInstrument());
        bitmexCurrentPriceInfoProviderSpy = spy(new BitmexCurrentPriceInfoProvider(instrumentServiceSpy));

        quotes = json.deserialize(Resources.toString(Resources.getResource("currentPrice.json"), StandardCharsets.UTF_8),
            new TypeToken<List<Quote>>() {
            }.getType());

        when(quoteApi.quoteGet(
            eq(BitmexUtils.getSymbol(INSTRUMENT)),
            isNull(),
            isNull(),
            eq(BigDecimal.ONE),
            isNull(),
            eq(true),
            isNull(),
            isNull())
        ).thenReturn(quotes);

        doReturn(quoteApi).when(bitmexCurrentPriceInfoProviderSpy).getQuoteApi();
    }

    @Test
    public void testGetCurrentPricesForInstrumentTest() {
        Price price = bitmexCurrentPriceInfoProviderSpy.getCurrentPricesForInstrument(INSTRUMENT);
        assertThat(price.getInstrument().getInstrument()).isEqualTo(price.getInstrument().getInstrument());
        assertThat(price.getAskPrice()).isCloseTo(quotes.get(0).getAskPrice(), Offset.offset(0.0001));
        assertThat(price.getBidPrice()).isCloseTo(quotes.get(0).getBidPrice(), Offset.offset(0.0001));
        assertThat(price.getPricePoint()).isEqualTo(DATE_TIME_FORMATTER.parseDateTime("2020-10-24T16:57:00.615Z"));
    }

    @Test
    public void testGetCurrentPricesForInstruments() {
        Map<TradeableInstrument, Price> prices =
            bitmexCurrentPriceInfoProviderSpy.getCurrentPricesForInstruments(Collections.singletonList(INSTRUMENT));
        assertThat(prices).hasSize(1);

        Price price = prices.get(INSTRUMENT);
        assertThat(price).isNotNull();
        assertThat(price.getInstrument().getInstrument()).isEqualTo(price.getInstrument().getInstrument());
        assertThat(price.getAskPrice()).isCloseTo(quotes.get(0).getAskPrice(), Offset.offset(0.0001));
        assertThat(price.getBidPrice()).isCloseTo(quotes.get(0).getBidPrice(), Offset.offset(0.0001));
        assertThat(price.getPricePoint()).isEqualTo(DATE_TIME_FORMATTER.parseDateTime("2020-10-24T16:57:00.615Z"));
    }
}
