package com.tradebot.bitmex.restapi.trade;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.TradeApi;
import com.tradebot.bitmex.restapi.generated.model.Trade;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.trade.TradeManagementProvider;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
public class BitmexTradeManagementProvider implements TradeManagementProvider<String, Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final TradeApi tradeApi = new TradeApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );

    @Override
    public boolean modifyTrade(Long accountId, String tradeId, double stopLoss, double takeProfit) {
        throw new NotImplementedException("Trade cannot be modified");
    }

    @Override
    public boolean closeTrade(String tradeId, Long accountId) {
        throw new NotImplementedException("Trade cannot be closed");
    }

    @Override
    @SneakyThrows
    public com.tradebot.core.trade.Trade<String, Long> getTradeForAccount(String tradeId, Long accountId) {
        return getAllTrades().stream()
            .filter(trade -> trade.getTrdMatchID().equals(tradeId))
            .map(trade -> convertTo(accountId, trade))
            .findAny().orElseThrow();
    }

    @Override
    @SneakyThrows
    public Collection<com.tradebot.core.trade.Trade<String, Long>> getTradesForAccount(Long accountId) {
        return getAllTrades().stream().map(trade -> convertTo(accountId, trade)).collect(Collectors.toList());
    }

    private List<Trade> getAllTrades() throws ApiException {
        return getTradeApi().tradeGet(
            null,
            null,
            null,
            BigDecimal.valueOf(bitmexAccountConfiguration.getBitmex().getApi().getTradesDepth()),
            BigDecimal.ZERO,
            true,
            null,
            null
        );
    }

    private com.tradebot.core.trade.Trade<String, Long> convertTo(Long accountId, Trade trade) {
        return new com.tradebot.core.trade.Trade(
            trade.getTrdMatchID(),
            trade.getSize().longValue(),
            TradingSignalConvertible.fromString(trade.getSide()),
            new TradeableInstrument(trade.getSymbol(), trade.getSymbol()),
            trade.getTimestamp(),
            0.0,
            trade.getPrice(),
            0.0,
            accountId
        );
    }

}
