package com.tradebot.bitmex.restapi.position;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.model.Order;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.ApiResponse;
import com.tradebot.bitmex.restapi.model.BitmexOperationQuotas;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultContext;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.position.PositionManagementProvider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;


@Slf4j
public class BitmexPositionManagementProvider implements PositionManagementProvider<Long> {

    private static final String POSITION_ERROR = "Position error {} {}";

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
    public OperationResultContext<com.tradebot.core.position.Position> getPositionForInstrument(Long accountId, TradeableInstrument instrument) {
        try {
            ApiResponse<List<Position>> apiAllPositionsResponse = getAllPositions();
            List<Position> positions = apiAllPositionsResponse.getData();

            com.tradebot.core.position.Position filteredPositions =
                positions != null ? positions.stream()
                    .filter(position -> position.getAccount().longValue() == accountId)
                    .filter(position -> position.getSymbol().equals(instrument.getInstrument()))
                    .findAny().map(this::toPosition).orElse(null) : null;

            return BitmexUtils.prepareResultReturned(apiAllPositionsResponse, new BitmexOperationQuotas<>(filteredPositions));
        } catch (ApiException apiException) {
            log.error(POSITION_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<Collection<com.tradebot.core.position.Position>> getPositionsForAccount(Long accountId) {
        try {
            ApiResponse<List<Position>> apiAllPositionsResponse = getAllPositions();
            List<Position> positions = apiAllPositionsResponse.getData();

            Collection<com.tradebot.core.position.Position> filteredPositions =
                positions != null ? positions.stream()
                    .filter(position -> position.getAccount().longValue() == accountId)
                    .map(this::toPosition).collect(Collectors.toList()) : null;

            return BitmexUtils.prepareResultReturned(apiAllPositionsResponse, new BitmexOperationQuotas<>(filteredPositions));

        } catch (ApiException apiException) {
            log.error(POSITION_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    @Override
    public OperationResultContext<String> closePosition(Long accountId, TradeableInstrument instrument, double price) {
        try {
            ApiResponse<Order> apiClosePositionResponse =
                getOrderApi().orderClosePositionWithHttpInfo(instrument.getInstrument(), price <= 0 ? null : price);

            Order order = apiClosePositionResponse.getData();
            String orderId = order != null ? order.getOrderID() : null;
            return BitmexUtils.prepareResultReturned(apiClosePositionResponse, new BitmexOperationQuotas<>(orderId));

        } catch (ApiException apiException) {
            log.error(POSITION_ERROR, apiException.getResponseBody(), ExceptionUtils.getMessage(apiException));
            return new OperationResultContext<>(null, BitmexUtils.errorMessageFromApiException(apiException));
        }
    }

    private ApiResponse<List<Position>> getAllPositions() throws ApiException {
        return getPositionApi().positionGetWithHttpInfo(null, null, null);
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
