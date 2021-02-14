package com.tradebot.stepdefs;

import com.tradebot.core.helper.CacheCandlestick;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TradeBotAppDefinitions {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private final TradeableInstrument tradeableInstrument = new TradeableInstrument("USDJPY", "USDJPY", 0.001, null, null, null, null, null);

    private CacheCandlestick cacheCandlestick;

    @Given("^I have empty cache$")
    public void i_have_empty_cache() throws Exception {
        cacheCandlestick = new CacheCandlestick(tradeableInstrument, 10,
            Collections.singletonList(CandleStickGranularity.H1));
    }

    @When("^I added history$")
    public void i_added_history() throws Exception {
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
    }

    @Then("^I will be able to read history$")
    public void i_will_be_able_to_read_history() throws Exception {
        Map<DateTime, CandleStick> result = cacheCandlestick.getValuesForGranularity(CandleStickGranularity.H1);
    }

}
