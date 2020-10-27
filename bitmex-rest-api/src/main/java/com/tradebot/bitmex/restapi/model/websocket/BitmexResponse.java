package com.tradebot.bitmex.restapi.model.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BitmexResponse<T> {
    private T[] data;
    private String table;
}
