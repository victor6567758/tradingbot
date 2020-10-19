
package com.tradebot.core.instrument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.tradebot.core.TradingTestConstants;

public class InstrumentServiceTest {

	private final double NON_JPY_PIP = 0.0001;
	private final double JPY_PIP = 0.01;

	@SuppressWarnings("unchecked")
	@Test
	public void testAll() {
		InstrumentDataProvider<String> instrumentDataProvider = mock(InstrumentDataProvider.class);
		Collection<TradeableInstrument<String>> instruments = createInstruments();

		when(instrumentDataProvider.getInstruments()).thenReturn(instruments);
		InstrumentService<String> service = new InstrumentService<>(instrumentDataProvider);
		Collection<TradeableInstrument<String>> usdpairs = service.getAllPairsWithCurrency("USD");
		assertEquals(4, usdpairs.size());

		TradeableInstrument<String> usdjpy = new TradeableInstrument<>("USD_JPY");
		assertTrue(usdpairs.contains(usdjpy));

		Collection<TradeableInstrument<String>> jpypairs = service.getAllPairsWithCurrency("JPY");
		assertEquals(2, jpypairs.size());
		assertThat(jpypairs).contains(usdjpy);

		Collection<TradeableInstrument<String>> xaupairs = service.getAllPairsWithCurrency("XAU");
		assertTrue(xaupairs.isEmpty());

		Collection<TradeableInstrument<String>> nullpairs = service.getAllPairsWithCurrency(null);
		assertTrue(nullpairs.isEmpty());
		assertEquals(NON_JPY_PIP, service.getPipForInstrument(new TradeableInstrument<>("GBP_CHF")),
				TradingTestConstants.PRECISION);
		assertEquals(JPY_PIP, service.getPipForInstrument(usdjpy), TradingTestConstants.PRECISION);
		assertEquals(1.0, service.getPipForInstrument(new TradeableInstrument<>("XAU_EUR")),
				TradingTestConstants.PRECISION);
	}

	@Test
	public void equalityTest() {
		TradeableInstrument<Long> usdjpy1 = new TradeableInstrument<>("USD_JPY", 10001L, "USDJPY currency pair");
		TradeableInstrument<Long> usdjpy2 = new TradeableInstrument<>("USD_JPY", 10002L, "USDJPY currency pair");
		TradeableInstrument<Long> usdjpy3 = new TradeableInstrument<>("USD_JPY", 10001L, "USDJPY currency pair");
		TradeableInstrument<Long> usdchf1 = new TradeableInstrument<>("USD_CHF", 10003L, "USDCHF currency pair");

		assertThat(usdjpy1).isEqualTo(usdjpy3);
		assertThat(usdjpy1).isNotEqualTo(usdjpy2);
		assertThat(usdjpy3).isNotEqualTo(usdchf1);
	}

	private Collection<TradeableInstrument<String>> createInstruments() {
		Collection<TradeableInstrument<String>> instruments = Lists.newArrayList();

		instruments.add(new TradeableInstrument<>("GBP_USD", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<>("GBP_CHF", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<>("EUR_USD", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<>("NZD_USD", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<>("USD_JPY", JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument<>("AUD_JPY", JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		return instruments;
	}
}
