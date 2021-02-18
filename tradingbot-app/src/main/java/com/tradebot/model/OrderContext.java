package com.tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderContext {

    private String orderClientId;
    private double levelPrice;
}
