package com.tradebot.bitmex.restapi.model.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitmexTrade {

    private String symbol;
    private String side;
    private double price;
    private double size;
    private String timestamp;
}
