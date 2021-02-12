package com.tradebot.bitmex.restapi.marketdata;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.QuoteApi;
import com.tradebot.bitmex.restapi.generated.model.Quote;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BitmexCurrentPriceInfoProvider implements CurrentPriceInfoProvider {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final QuoteApi quoteApi = new QuoteApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public Map<TradeableInstrument, Price> getCurrentPricesForInstruments(
        Collection<TradeableInstrument> tradeableInstruments) {

        return tradeableInstruments.stream()
            .map(this::getCurrentQuote)
            .collect(Collectors.toMap(Price::getInstrument,
                Function.identity(), (first, second) -> first));
    }

    @Override
    public Price getCurrentPricesForInstrument(TradeableInstrument instrument) {
        return getCurrentQuote(instrument);
    }

    private Price getCurrentQuote(TradeableInstrument instrument) {

        try {
            List<Quote> quotes = getQuoteApi().quoteGet(BitmexUtils.getSymbol(instrument), null, null, BigDecimal.ONE, null, true, null, null);
            if (quotes.size() > 1) {
                log.warn("More than 1 quote returned");
            }

            Quote quote = quotes.get(0);
            return new Price(
                new TradeableInstrument(quote.getSymbol(), quote.getSymbol()),
                quote.getBidPrice(),
                quote.getAskPrice(),
                quote.getTimestamp()
            );

        } catch (
            ApiException apiException) {
            throw new IllegalArgumentException(apiException);
        }
    }
}
