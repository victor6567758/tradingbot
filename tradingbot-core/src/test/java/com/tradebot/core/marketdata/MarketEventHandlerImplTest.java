
package com.tradebot.core.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.instrument.TradeableInstrument;

public class MarketEventHandlerImplTest {

	private class MarketDataSubscriber {

		@Subscribe
		@AllowConcurrentEvents
		public void processPayLoad(MarketDataPayLoad payload) {
			assertEquals(GBPUSD, payload.getInstrument().getInstrument());
			assertEquals(BID, payload.getBidPrice(), TradingTestConstants.PRECISION);
			assertEquals(ASK, payload.getAskPrice(), TradingTestConstants.PRECISION);
			done.countDown();
		}
	}

	private static final double BID = 1.55;
	private static final double ASK = 1.5502;
	private static final String GBPUSD = "GBP_USD";
	private static final int NUM_SUBSCRIBERS = 2;

	private final CountDownLatch done = new CountDownLatch(NUM_SUBSCRIBERS);

	@Test
	public void testRideEventBus() throws Exception {
		EventBus evtBus = new AsyncEventBus(Executors.newFixedThreadPool(NUM_SUBSCRIBERS));
		for (int i = 1; i <= NUM_SUBSCRIBERS; i++) {
			evtBus.register(new MarketDataSubscriber());
		}
		MarketEventCallback callback = new MarketEventHandlerImpl(evtBus);
		callback.onMarketEvent(new TradeableInstrument(GBPUSD, GBPUSD), BID, ASK, DateTime.now());
		done.await();
		assertThat(done.getCount()).isZero();
	}


}
