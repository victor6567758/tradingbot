
package com.tradebot.core.events;


public interface EventPayLoadToTweet<K, T extends EventPayLoad<K>> {

    String toTweet(T payLoad);
}
