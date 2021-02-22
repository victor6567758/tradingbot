
package com.tradebot.core.heartbeats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.tradebot.core.streaming.heartbeats.HeartBeatStreamingService;

public class DefaultHeartBeatServiceTest {

	private final static String TESTSTREAM = "TESTSTREAM";

	HeartBeatStreamingService heartBeatStreamingService;

	@Before
	public void init() {
		heartBeatStreamingService = mock(HeartBeatStreamingService.class);
		when(heartBeatStreamingService.getHeartBeatSourceId()).thenReturn(TESTSTREAM);
	}

	@Test
	public void heartBeatTest() {

		DefaultHeartBeatService service = new DefaultHeartBeatService(Arrays.asList(heartBeatStreamingService), 1L, 1L);
		service.init();

		EventBus eventBus = new EventBus();
		eventBus.register(service);

		HeartBeatCallback<DateTime> heartBeatCallBack = new HeartBeatCallbackImpl<>(eventBus);
		DateTime now = DateTime.now();

		heartBeatCallBack.onHeartBeat(new HeartBeatPayLoad<>(now.minusMinutes(2), TESTSTREAM));
		verify(heartBeatStreamingService, atLeastOnce()).startHeartBeatStreaming();
		heartBeatCallBack.onHeartBeat(new HeartBeatPayLoad<>(DateTime.now(), TESTSTREAM));

		service.stop();
		assertThat(service.isTerminated()).isTrue();
	}
}
