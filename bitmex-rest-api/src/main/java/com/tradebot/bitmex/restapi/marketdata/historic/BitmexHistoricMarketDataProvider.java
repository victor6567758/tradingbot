package com.tradebot.bitmex.restapi.marketdata.historic;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.TradeApi;
import com.tradebot.bitmex.restapi.generated.model.TradeBin;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.historic.CandleStick;
import com.tradebot.core.marketdata.historic.CandleStickGranularity;
import com.tradebot.core.marketdata.historic.HistoricMarketDataProvider;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

@Slf4j
public class BitmexHistoricMarketDataProvider implements HistoricMarketDataProvider {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    private final InstrumentService instrumentService;

    public BitmexHistoricMarketDataProvider(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Getter(AccessLevel.PACKAGE)
    private final TradeApi tradeApi = new TradeApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public List<CandleStick> getCandleSticks(
        TradeableInstrument instrument,
        CandleStickGranularity granularity,
        DateTime from,
        DateTime to) {

        try {
            return getTradeApi().tradeGetBucketed(
                BitMexGranularity.toBitmexGranularity(granularity), true, instrument.getInstrument(), null, null,
                BigDecimal.valueOf(bitmexAccountConfiguration.getBitmex().getApi().getHistoryDepth()),
                null, true, from, to).stream().map(
                bucket -> toCandleStick(bucket, granularity)).collect(Collectors.toList());

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }

    }

    @Override
    public List<CandleStick> getCandleSticks(
        TradeableInstrument instrument,
        CandleStickGranularity granularity,
        int count) {

        try {
            return getTradeApi().tradeGetBucketed(
                BitMexGranularity.toBitmexGranularity(granularity),
                true,
                instrument.getInstrument(),
                null,
                null,
                BigDecimal.valueOf(bitmexAccountConfiguration.getBitmex().getApi().getHistoryDepth()),
                null,
                true,
                null,
                null
            ).stream().map(
                bucket -> toCandleStick(bucket, granularity)).collect(Collectors.toList());

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    private CandleStick toCandleStick(TradeBin tradeBin, CandleStickGranularity granularity) {
        return new CandleStick(
            tradeBin.getOpen(),
            tradeBin.getHigh(),
            tradeBin.getLow(),
            tradeBin.getClose(),
            tradeBin.getTimestamp(),
            instrumentService.resolveTradeableInstrument(tradeBin.getSymbol()),
            granularity
        );
    }


}
