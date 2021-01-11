package com.tradebot.core.helper;

public interface ProviderHelper<T> {


    String fromIsoFormat(String instrument);

    String toIsoFormat(String instrument);

    String fromPairSeparatorFormat(String instrument);

    T getLongNotation();

    T getShortNotation();
}
