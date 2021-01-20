package com.tradebot.core.instrument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class InstrumentPairInterestRate {

    private final double baseCurrencyBidInterestRate;
    private final double baseCurrencyAskInterestRate;
    private final double quoteCurrencyBidInterestRate;
    private final double quoteCurrencyAskInterestRate;


}
