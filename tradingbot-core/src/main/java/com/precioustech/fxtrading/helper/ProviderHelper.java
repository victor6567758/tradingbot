package com.precioustech.fxtrading.helper;

public interface ProviderHelper<T> {


    String fromIsoFormat(String instrument);

    String toIsoFormat(String instrument);

    String fromPairSeparatorFormat(String instrument);

    String fromHashTagCurrency(String instrument);

    T getLongNotation();

    T getShortNotation();
}
