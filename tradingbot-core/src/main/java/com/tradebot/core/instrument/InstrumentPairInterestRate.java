package com.tradebot.core.instrument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class InstrumentPairInterestRate {

    private final Double baseCurrencyBidInterestRate;
    private final Double baseCurrencyAskInterestRate;
    private final Double quoteCurrencyBidInterestRate;
    private final Double quoteCurrencyAskInterestRate;


}
