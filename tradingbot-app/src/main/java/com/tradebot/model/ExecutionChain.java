package com.tradebot.model;

import com.tradebot.bitmex.restapi.model.BitmexExecution;
import com.tradebot.core.order.Order;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class ExecutionChain {

    private final int level;
    private final List<Pair<Order, List<BitmexExecution>>> chain = new ArrayList<>();
}
