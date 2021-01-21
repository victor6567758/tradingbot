package com.tradebot.bitmex.restapi.config;


import com.tradebot.core.BaseTradingConfig;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class BitmexAccountConfiguration {

    @Data
    public static class Bitmex {

        private Api api;
        private Db db;
        private TradingConfiguration tradingConfiguration;
    }

    @Data
    public static class Api {

        private String key;
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
    public static class Db {

        private String driverClassName;
        private String url;
        private String user;
        private String pass;

    }

    @Data
    public static class TradingConfiguration extends BaseTradingConfig {

        private String mailTo;
        private int priceExpiryMinutes;
        private List<Map<String, ?>> tradeableInstruments;
    }

    private Bitmex bitmex;
}
