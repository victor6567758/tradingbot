package com.tradebot.core.instrument.economicevent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.joda.time.DateTime;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class InstrumentEconomicEvent {
    private final DateTime eventDate;
    private final String currency;
    private final String eventDescription;
    private final InstrumentEconomicEventImpact eventImpact;
    private final String previous;
    private final String actual;
    private final String forecast;
}
