package com.tradebot.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

public class CacheCandlestickTest {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");


    @Test
    public void addHistoryTest() {
        TradeableInstrument tradeableInstrument = new TradeableInstrument("USDJPY", "USDJPY");

        CacheCandlestick cacheCandlestick = new CacheCandlestick(tradeableInstrument, 10,
            Collections.singletonList(CandleStickGranularity.H1));

        CandleStick candleStick1 = new CandleStick(1.0, 2.0, 0.5, 2.1, DATETIME_FORMATTER.parseDateTime("01/01/2020 01:00:00"),
            tradeableInstrument,
            CandleStickGranularity.H1);

        CandleStick candleStick2 = new CandleStick(2.1, 2.8, 1.9, 2.5, DATETIME_FORMATTER.parseDateTime("01/01/2020 02:00:00"),
            tradeableInstrument,
            CandleStickGranularity.H1);

        cacheCandlestick.addHistory(tradeableInstrument, Arrays.asList(
            candleStick1,
            candleStick2
        ));

        Map<DateTime, CandleStick> result = cacheCandlestick.getValuesForGranularity(CandleStickGranularity.H1);
        assertThat(result.keySet()).containsExactlyInAnyOrder(candleStick1.getEventDate(), candleStick2.getEventDate());
        assertThat(result.values()).containsExactlyInAnyOrder(candleStick1, candleStick2);
    }
}
