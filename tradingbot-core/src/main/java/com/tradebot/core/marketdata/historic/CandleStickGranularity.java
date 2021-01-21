
package com.tradebot.core.marketdata.historic;

public enum CandleStickGranularity {
    S0(-1, "Dummy"),
    S5(5, "5 seconds"),
    S10(10, "10 seconds"),
    S15(15, "15 seconds"),
    S30(30, "30 seconds"),
    M1(60 * 1L, "1 minute"),
    M2(60 * 2L, "2 minutes"),
    M3(60 * 3L, "3 minutes"),
    M5(60 * 5L, "5 minutes"),
    M10(60 * 10L, "10 minutes"),
    M15(60 * 15L, "15 minutes"),
    M30(60 * 30L, "30 minutes"),
    H1(60 * 60L, "1 hour"),
    H2(60 * 60 * 2L, "2 hours"),
    H3(60 * 60 * 3L, "3 hours"),
    H4(60 * 60 * 4L, "4 hours"),
    H6(60 * 60 * 6L, "6 hours"),
    H8(60 * 60 * 8L, "8 hours"),
    H12(60 * 60 * 12L, "12 hours"),
    D(60 * 60 * 24L, "1 day"),
    W(60 * 60 * 24L * 7, "1 week"),
    M(60 * 60 * 24L * 30, "1 month");

    private final long granularityInSeconds;
    private final String label;

    CandleStickGranularity(long granularityInSeconds, String label) {
        this.granularityInSeconds = granularityInSeconds;
        this.label = label;
    }


    public long getGranularityInSeconds() {
        return granularityInSeconds;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name();
    }
}
