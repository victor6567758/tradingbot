package com.tradebot.core.instrument;


import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TradeableInstrument<T> {
    private final String instrument;
    private final String description;
    private final T instrumentId;
    private final double pip;
    private final int hash;
    private final InstrumentPairInterestRate instrumentPairInterestRate;

    public TradeableInstrument(String instrument) {
        this(instrument, null, 0.0, null, instrument);
    }

    public TradeableInstrument(String instrument, double pip, InstrumentPairInterestRate instrumentPairInterestRate, String description) {
        this(instrument, null, pip, instrumentPairInterestRate, description);
    }

    public TradeableInstrument(
        String instrument,
        T instrumentId,
        double pip,
        InstrumentPairInterestRate instrumentPairInterestRate,
        String description) {

        this.instrument = instrument;
        this.pip = pip;
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
    @SuppressWarnings("unchecked")
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
        TradeableInstrument<T> other = (TradeableInstrument<T>) obj;
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
