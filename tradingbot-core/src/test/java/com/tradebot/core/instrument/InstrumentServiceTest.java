
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
		InstrumentDataProvider instrumentDataProvider = mock(InstrumentDataProvider.class);
		Collection<TradeableInstrument> instruments = createInstruments();

		when(instrumentDataProvider.getInstruments()).thenReturn(instruments);
		InstrumentService service = new InstrumentService(instrumentDataProvider);
		Collection<TradeableInstrument> usdpairs = service.getAllPairsWithCurrency("USD");
		assertEquals(4, usdpairs.size());

		TradeableInstrument usdjpy = new TradeableInstrument("USD_JPY", "USD_JPY");
		assertTrue(usdpairs.contains(usdjpy));

		Collection<TradeableInstrument> jpypairs = service.getAllPairsWithCurrency("JPY");
		assertEquals(2, jpypairs.size());
		assertThat(jpypairs).contains(usdjpy);

		Collection<TradeableInstrument> xaupairs = service.getAllPairsWithCurrency("XAU");
		assertTrue(xaupairs.isEmpty());

		Collection<TradeableInstrument> nullpairs = service.getAllPairsWithCurrency(null);
		assertTrue(nullpairs.isEmpty());
		assertEquals(NON_JPY_PIP, service.getPipForInstrument(new TradeableInstrument("GBP_CHF", "GBP_CHF")),
				TradingTestConstants.PRECISION);
		assertEquals(JPY_PIP, service.getPipForInstrument(usdjpy), TradingTestConstants.PRECISION);
		assertEquals(1.0, service.getPipForInstrument(new TradeableInstrument("XAU_EUR", "XAU_EUR")),
				TradingTestConstants.PRECISION);
	}

	@Test
	public void testEquality() {
		TradeableInstrument usdjpy1 = new TradeableInstrument("USD_JPY", "USD_JPY", 0.0, null, "USDJPY currency pair");
		TradeableInstrument usdjpy2 = new TradeableInstrument("USD_JPY", "USD_JPY", 0.0, null, "USDJPY currency pair");
		TradeableInstrument usdjpy3 = new TradeableInstrument("USD_JPY", "USD_JPY", 0.0, null, "USDJPY currency pair");
		TradeableInstrument usdchf1 = new TradeableInstrument("USD_CHF", "USD_CHF", 0.0, null, "USDCHF currency pair");

		assertThat(usdjpy1).isEqualTo(usdjpy3);
		assertThat(usdjpy1).isEqualTo(usdjpy2);
		assertThat(usdjpy3).isNotEqualTo(usdchf1);
	}

	private Collection<TradeableInstrument> createInstruments() {
		Collection<TradeableInstrument> instruments = Lists.newArrayList();

		instruments.add(new TradeableInstrument("GBP_USD", "GBP_USD", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument("GBP_CHF", "GBP_CHF", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument("EUR_USD", "EUR_USD", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument("NZD_USD", "NZD_USD", NON_JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument("USD_JPY", "USD_JPY", JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		instruments.add(new TradeableInstrument("AUD_JPY", "USD_JPY", JPY_PIP, mock(InstrumentPairInterestRate.class),
				StringUtils.EMPTY));
		return instruments;
	}
}
