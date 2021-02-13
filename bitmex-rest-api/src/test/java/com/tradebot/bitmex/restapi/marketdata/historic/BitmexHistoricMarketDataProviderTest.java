package com.tradebot.bitmex.restapi.marketdata.historic;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;
import com.tradebot.bitmex.restapi.generated.api.TradeApi;
import com.tradebot.bitmex.restapi.generated.model.TradeBin;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.JSON;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import org.assertj.core.data.Offset;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;


public class BitmexHistoricMarketDataProviderTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .append(ISODateTimeFormat.dateTime().getPrinter(), ISODateTimeFormat.dateOptionalTimeParser().getParser())
        .toFormatter();

    private static final TradeableInstrument XBTUSD_INSTR =
        new TradeableInstrument("XBTUSD", "XBTUSD", 0.5, null, null, BigDecimal.valueOf(1L), null, null);

    private static final BigDecimal HISTORY_DEPTH = BigDecimal.valueOf(100L);

    private final JSON json = new JSON();
    private final TradeApi tradeApi = mock(TradeApi.class);
    private BitmexHistoricMarketDataProvider bitmexHistoricMarketDataProviderSpy;
    private InstrumentService instrumentServiceSpy;

    private List<TradeBin> tradeBins1d;
    private List<TradeBin> tradeBins1m;
    private List<TradeBin> tradeBins1d20_10_2020;

    @Before
    public void init() throws ApiException, IOException {

        instrumentServiceSpy = mock(InstrumentService.class);
        doReturn(XBTUSD_INSTR).when(instrumentServiceSpy).resolveTradeableInstrument(XBTUSD_INSTR.getInstrument());

        bitmexHistoricMarketDataProviderSpy = spy(new BitmexHistoricMarketDataProvider(instrumentServiceSpy));

        tradeBins1d = json.deserialize(Resources.toString(Resources.getResource("tradeBulkReply1d.json"), StandardCharsets.UTF_8),
            new TypeToken<List<TradeBin>>() {
            }.getType());

        tradeBins1m = json.deserialize(Resources.toString(Resources.getResource("tradeBulkReply1m.json"), StandardCharsets.UTF_8),
            new TypeToken<List<TradeBin>>() {
            }.getType());

        tradeBins1d20_10_2020 = json.deserialize(Resources.toString(Resources.getResource("tradeBulkReply1d20_10_2020.json"), StandardCharsets.UTF_8),
            new TypeToken<List<TradeBin>>() {
            }.getType());

        when(tradeApi.tradeGetBucketed(
            eq("1m"),
            eq(true),
            eq(XBTUSD_INSTR.getInstrument()),
            isNull(),
            isNull(),
            eq(HISTORY_DEPTH),
            isNull(),
            eq(true),
            isNull(),
            isNull())
        ).thenReturn(tradeBins1m);

        when(tradeApi.tradeGetBucketed(
            eq("1d"),
            eq(true),
            eq(XBTUSD_INSTR.getInstrument()),
            isNull(),
            isNull(),
            eq(HISTORY_DEPTH),
            isNull(),
            eq(true),
            isNull(),
            isNull())
        ).thenReturn(tradeBins1d);

        when(tradeApi.tradeGetBucketed(
            eq("1d"),
            eq(true),
            eq(XBTUSD_INSTR.getInstrument()),
            isNull(),
            isNull(),
            eq(HISTORY_DEPTH),
            isNull(),
            eq(true),
            eq(DATE_TIME_FORMATTER.parseDateTime("2020-10-20T00:00:00.000Z")),
            eq(DATE_TIME_FORMATTER.parseDateTime("2020-10-20T00:00:00.000Z")))
        ).thenReturn(tradeBins1d20_10_2020);

        doReturn(tradeApi).when(bitmexHistoricMarketDataProviderSpy).getTradeApi();
    }

    @Test
    public void testGetCandleSticks1M() {
        List<CandleStick> candles =
            bitmexHistoricMarketDataProviderSpy.getCandleSticks(XBTUSD_INSTR, CandleStickGranularity.M1, HISTORY_DEPTH.intValue());

        assertThat(candles.size()).isEqualTo(HISTORY_DEPTH.intValue());
        assertThat(candles.stream().map(n -> n.getInstrument().getInstrument()).distinct().filter(n -> XBTUSD_INSTR.getInstrument().equals(n))
            .count()).isEqualTo(1);

        assertThat(candles.stream().map(CandleStick::getEventDate).max(Comparator.naturalOrder()).get())
            .isEqualTo(DATE_TIME_FORMATTER.parseDateTime("2020-10-24T11:37:00.000Z"));

        assertThat(candles.stream().map(CandleStick::getEventDate).min(Comparator.naturalOrder()).get())
            .isEqualTo(DATE_TIME_FORMATTER.parseDateTime("2020-10-24T09:58:00.000Z"));

        assertThat(candles.stream().map(CandleStick::getOpenPrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
        assertThat(candles.stream().map(CandleStick::getClosePrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
        assertThat(candles.stream().map(CandleStick::getHighPrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
        assertThat(candles.stream().map(CandleStick::getLowPrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
    }

    @Test
    public void testGetCandleSticks1D() {
        List<CandleStick> candles =
            bitmexHistoricMarketDataProviderSpy.getCandleSticks(XBTUSD_INSTR, CandleStickGranularity.D, HISTORY_DEPTH.intValue());

        assertThat(candles.size()).isEqualTo(HISTORY_DEPTH.intValue());
        assertThat(candles.stream().map(n -> n.getInstrument().getInstrument()).distinct().filter(n -> XBTUSD_INSTR.getInstrument().equals(n))
            .count()).isEqualTo(1);

        assertThat(candles.stream().map(CandleStick::getEventDate).max(Comparator.naturalOrder()).get())
            .isEqualTo(DATE_TIME_FORMATTER.parseDateTime("2020-10-25T00:00:00.000Z"));

        assertThat(candles.stream().map(CandleStick::getEventDate).min(Comparator.naturalOrder()).get())
            .isEqualTo(DATE_TIME_FORMATTER.parseDateTime("2020-07-18T00:00:00.000Z"));

        assertThat(candles.stream().map(CandleStick::getOpenPrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
        assertThat(candles.stream().map(CandleStick::getClosePrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
        assertThat(candles.stream().map(CandleStick::getHighPrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);
        assertThat(candles.stream().map(CandleStick::getLowPrice).min(Comparator.naturalOrder()).get()).isGreaterThan(0.0);

    }

    @Test
    public void testGetCandleSticks1DFiltered() {
        List<CandleStick> candles =
            bitmexHistoricMarketDataProviderSpy.getCandleSticks(XBTUSD_INSTR, CandleStickGranularity.D,
                DATE_TIME_FORMATTER.parseDateTime("2020-10-20T00:00:00.000Z"),
                DATE_TIME_FORMATTER.parseDateTime("2020-10-20T00:00:00.000Z"));

        assertThat(candles.size()).isEqualTo(1);
        CandleStick dayCandle = candles.get(0);

        assertThat(dayCandle.getInstrument().getInstrument()).isEqualTo(XBTUSD_INSTR.getInstrument());
        assertThat(dayCandle.getHighPrice()).isCloseTo(tradeBins1d20_10_2020.get(0).getHigh(), Offset.offset(0.00001));
        assertThat(dayCandle.getLowPrice()).isCloseTo(tradeBins1d20_10_2020.get(0).getLow(), Offset.offset(0.00001));
        assertThat(dayCandle.getOpenPrice()).isCloseTo(tradeBins1d20_10_2020.get(0).getOpen(), Offset.offset(0.00001));
        assertThat(dayCandle.getClosePrice()).isCloseTo(tradeBins1d20_10_2020.get(0).getClose(), Offset.offset(0.00001));

    }


}
