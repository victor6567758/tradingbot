package com.tradebot.bitmex.restapi.position;

import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.position.PositionManagementProvider;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BitmexPositionManagementProvider implements PositionManagementProvider<Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexConfiguration();

    private final InstrumentService instrumentService;

    public BitmexPositionManagementProvider(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }


    @Getter(AccessLevel.PACKAGE)
    private final PositionApi positionApi = new PositionApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Getter(AccessLevel.PACKAGE)
    private final OrderApi orderApi = new OrderApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Override
    public com.tradebot.core.position.Position getPositionForInstrument(Long accountId, TradeableInstrument instrument) {
        try {
            return getPositionApi().positionGet(null, null, null).stream()
                .filter(position -> position.getAccount().longValue() == accountId)
                .filter(position -> position.getSymbol().equals(instrument.getInstrument()))
                .map(this::toPosition).findAny().orElseThrow();
        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public Collection<com.tradebot.core.position.Position> getPositionsForAccount(Long accountId) {
        try {
            return getPositionApi().positionGet(null, null, null).stream()
                .filter(position -> position.getAccount().longValue() == accountId)
                .map(this::toPosition).collect(Collectors.toList());

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    @Override
    public boolean closePosition(Long accountId, TradeableInstrument instrument, double price) {
        try {
            getOrderApi().orderClosePosition(instrument.getInstrument(), price <= 0 ? null : price);
            return true;

        } catch (ApiException apiException) {
            throw new IllegalArgumentException(String.format(BitmexConstants.BITMEX_FAILURE,
                apiException.getResponseBody()), apiException);
        }
    }

    private com.tradebot.core.position.Position toPosition(Position position) {

        return new com.tradebot.core.position.Position(
            instrumentService.resolveTradeableInstrument(position.getSymbol()),
            position.getCurrentQty().longValue(),
            position.getCurrentQty().longValue() > 0 ? TradingSignal.LONG : TradingSignal.SHORT,
            position.getAvgCostPrice()
        );

    }
}
