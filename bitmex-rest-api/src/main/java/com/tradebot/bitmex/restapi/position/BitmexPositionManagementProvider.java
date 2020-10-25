package com.tradebot.bitmex.restapi.position;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.PositionApi;
import com.tradebot.bitmex.restapi.generated.model.Position;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.position.PositionManagementProvider;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;


@Slf4j
public class BitmexPositionManagementProvider implements PositionManagementProvider<String, Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final PositionApi positionApi = new PositionApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Override
    @SneakyThrows
    public com.tradebot.core.position.Position<String> getPositionForInstrument(Long accountId, TradeableInstrument<String> instrument) {
          return getPositionApi().positionGet(null, null, null).stream()
            .filter(position -> position.getAccount().longValue() == accountId)
            .filter(position -> position.getSymbol().equals(instrument.getInstrument()))
            .map(BitmexPositionManagementProvider::toPosition).findAny().orElseThrow();
    }

    @Override
    @SneakyThrows
    public Collection<com.tradebot.core.position.Position<String>> getPositionsForAccount(Long accountId) {
        return getPositionApi().positionGet(null, null, null).stream()
            .filter(position -> position.getAccount().longValue() == accountId)
            .map(BitmexPositionManagementProvider::toPosition).collect(Collectors.toList());
    }

    @Override
    public boolean closePosition(Long accountId, TradeableInstrument<String> instrument) {
        throw new NotImplementedException("closePosition");
    }

    private static com.tradebot.core.position.Position<String> toPosition(Position position) {
        return new com.tradebot.core.position.Position<>(
            new TradeableInstrument<>(position.getSymbol()),
            position.getCurrentQty().longValue(),
            position.getCurrentQty().longValue() > 0 ? TradingSignal.LONG : TradingSignal.SHORT,
            position.getAvgCostPrice()
        );
    }
}
