package com.tradebot.bitmex.restapi.config;


import lombok.Data;

@Data
public class BitmexAccountConfiguration {

    @Data
    public static class Bitmex {

        private Api api;
        private Db db;
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

    private Bitmex bitmex;
}
