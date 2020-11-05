
package com.tradebot.bitmex.restapi.order;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.bitmex.restapi.generated.api.OrderApi;
import com.tradebot.bitmex.restapi.utils.ApiClientAuthorizeable;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderManagementProvider;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BitmexOrderManagementProvider implements OrderManagementProvider<Long, String, Long> {

    private final BitmexAccountConfiguration bitmexAccountConfiguration = BitmexUtils.readBitmexCredentials();

    @Getter(AccessLevel.PACKAGE)
    private final OrderApi userApi = new OrderApi(
        new ApiClientAuthorizeable(bitmexAccountConfiguration.getBitmex().getApi().getKey(),
            bitmexAccountConfiguration.getBitmex().getApi().getSecret())
    );


    @Override
    public Long placeOrder(Order<String, Long> order, Long accountId) {
        return null;
    }

    @Override
    public boolean modifyOrder(Order<String, Long> order, Long accountId) {
        return false;
    }

    @Override
    public boolean closeOrder(Long orderId, Long accountId) {
        return false;
    }

    @Override
    public Collection<Order<String, Long>> allPendingOrders() {
        return null;
    }

    @Override
    public Collection<Order<String, Long>> pendingOrdersForAccount(Long accountId) {
        return null;
    }

    @Override
    public Order<String, Long> pendingOrderForAccount(Long orderId, Long accountId) {
        return null;
    }

    @Override
    public Collection<Order<String, Long>> pendingOrdersForInstrument(TradeableInstrument<String> instrument) {
        return null;
    }
}
