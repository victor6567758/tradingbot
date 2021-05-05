package com.tradebot.bitmex.restapi.config;


import com.tradebot.core.model.BaseTradingConfig;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

@Data
public class BitmexAccountConfiguration {

    @Data
    public static class Bitmex {

        private String tag;
        private Api api;
        private Db db;
        private Elastic elastic;
        private TradingConfiguration tradingConfiguration;
    }

    @Data
    public static class Api {

        @ToString.Exclude
        private String key;
        @ToString.Exclude
        private String secret;
        private String email;
        private String mainCurrency;
        private int transactionsDepth;
        private int historyDepth;
        private int orderDepth;
        private int tradesDepth;
        private String webSocketUrl;
        private int limitOrdersPerMinute;
    }

    @Data
    public static class Elastic {
        String index;
        String url;
    }

    @Data
    public static class Db {

        private String driverClassName;
        private String url;
        private String user;

        @ToString.Exclude
        private String pass;

    }

    @Data
    public static class TradingConfiguration extends BaseTradingConfig {

        private int waitCancelAllOrdersSec;
        private int orderDelaySec;
        private long accountId;
        private String mailTo;
        private int priceExpiryMinutes;
        private int tradingSolutionsDepthMin;
        private int waitForOrderOperationReplySecs;
        private List<Map<String, ?>> tradeableInstruments;
    }

    private Bitmex bitmex;
}
