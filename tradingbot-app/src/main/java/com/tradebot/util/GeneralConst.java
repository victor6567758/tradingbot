package com.tradebot.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GeneralConst {
    public static final String WS_TOPIC_PUBLISH_TRADE_CONFIG = "/tradeconfig";
    public static final String WS_TOPIC_PUBLISH_CHARTS = "/charts";
    public static final String WS_TOPIC_QUOTAS = "/quotas";

    public static final String WS_SEND_PREFIX = "/ws";
    public static final String WS_WEBSOCKET_ENDPOINT = "/websocket";
    public static final String WS_SOCKJS_ENDPOINT = "/sockjs";
    public static final String WS_TOPIC = "/topic";
}
