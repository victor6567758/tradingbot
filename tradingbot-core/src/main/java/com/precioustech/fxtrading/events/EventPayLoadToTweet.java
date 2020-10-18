
package com.precioustech.fxtrading.events;


public interface EventPayLoadToTweet<K, T extends EventPayLoad<K>> {

    String toTweet(T payLoad);
}
