package com.tradebot.core.instrument;


import java.math.BigDecimal;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(onlyExplicitlyIncluded = true)
public class TradeableInstrument {
    @ToString.Include
    private final String instrument;
    private final String description;
    private final String instrumentId;
    private final double tickSize;
    private final int scale;
    private final BigDecimal lotSize;
    private final BigDecimal multiplier;
    private final String positionCurrency;
    private final int hash;
    private final InstrumentPairInterestRate instrumentPairInterestRate;

    public TradeableInstrument(
        String instrument,
        String instrumentId,
        double tickSize,
        InstrumentPairInterestRate instrumentPairInterestRate,
        String description,
        BigDecimal lotSize,
        BigDecimal multiplier,
        String positionCurrency) {

        this.instrument = instrument;
        this.tickSize = tickSize;
        this.scale = BigDecimal.valueOf(tickSize).scale();
        this.lotSize = lotSize;
        this.multiplier = multiplier;
        this.positionCurrency = positionCurrency;
        this.instrumentPairInterestRate = instrumentPairInterestRate;
        this.instrumentId = instrumentId;
        this.description = description;
        this.hash = calcHashCode();
    }

    private int calcHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instrument == null) ? 0 : instrument.hashCode());
        result = prime * result + ((instrumentId == null) ? 0 : instrumentId.hashCode());
        return result;
    }


    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TradeableInstrument other = (TradeableInstrument) obj;
        if (instrument == null) {
            if (other.instrument != null) {
                return false;
            }
        } else if (!instrument.equals(other.instrument)) {
            return false;
        }
        if (instrumentId == null) {
            return other.instrumentId == null;
        } else {
            return instrumentId.equals(other.instrumentId);
        }
    }

}
