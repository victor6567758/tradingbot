
package com.precioustech.fxtrading.marketdata.historic;

public enum CandleStickGranularity {

    S5(5, "5 seconds"),
    S10(10, "10 seconds"),
    S15(15, "15 seconds"),
    S30(30, "30 seconds"),
    M1(60 * 1, "1 minute"),
    M2(60 * 2, "2 minutes"),
    M3(60 * 3, "3 minutes"),
    M5(60 * 5, "5 minutes"),
    M10(60 * 10, "10 minutes"),
    M15(60 * 15, "15 minutes"),
    M30(60 * 30, "30 minutes"),
    H1(60 * 60, "1 hour"),
    H2(60 * 60 * 2, "2 hours"),
    H3(60 * 60 * 3, "3 hours"),
    H4(60 * 60 * 4, "4 hours"),
    H6(60 * 60 * 6, "6 hours"),
    H8(60 * 60 * 8, "8 hours"),
    H12(60 * 60 * 12, "12 hours"),
    D(60 * 60 * 24, "1 day"),
    W(60 * 60 * 24 * 7, "1 week"),
    M(60 * 60 * 24 * 30, "1 month");

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
