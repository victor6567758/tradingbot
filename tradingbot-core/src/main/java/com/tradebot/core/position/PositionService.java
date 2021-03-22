package com.tradebot.core.position;

import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PositionService<K> {

    private static final String INVALID_POSITION_PROVIDER_RESULT_S = "Invalid order provider result: %s";
    private final PositionManagementProvider<K> positionManagementProvider;
    private final OperationResultCallback operationResultCallback;

    public Position getPositionForInstrument(K accountId, TradeableInstrument instrument) {
        OperationResultContext<Position> result = positionManagementProvider.getPositionForInstrument(accountId, instrument);
        operationResultCallback.onOrderResult(result);

        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_POSITION_PROVIDER_RESULT_S, result.getMessage()));
    }

    public Collection<Position> getPositionsForAccount(K accountId) {
        OperationResultContext<Collection<Position>> result = positionManagementProvider.getPositionsForAccount(accountId);
        operationResultCallback.onOrderResult(result);

        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_POSITION_PROVIDER_RESULT_S, result.getMessage()));
    }

    public String closePosition(K accountId, TradeableInstrument instrument, double price) {
        OperationResultContext<String> result = positionManagementProvider.closePosition(accountId, instrument, price);
        operationResultCallback.onOrderResult(result);

        if (result.isResult()) {
            return result.getData();
        }
        throw new IllegalArgumentException(String.format(INVALID_POSITION_PROVIDER_RESULT_S, result.getMessage()));
    }
}
