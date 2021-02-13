
package com.tradebot.core.marketdata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;

public class PipJumpCutOffCalculatorServiceTest {

	@SuppressWarnings("unchecked")
	@Test
	public void foo() {

		final double pip1 = 0.0001;
		final double pip2 = 0.01;

		CurrentPriceInfoProvider currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
		InstrumentService instrumentService = mock(InstrumentService.class);
		DateTime now = DateTime.now();

		TradeableInstrument eurusd = new TradeableInstrument("EUR_USD","EUR_USD");
		Price eurusdPrice = new Price(eurusd, 1.11905, 1.11915, now);

		TradeableInstrument nzdchf = new TradeableInstrument("NZD_CHF","NZD_CHF");
		Price nzdchfPrice = new Price(nzdchf, 0.65382, 0.65402, now);

		TradeableInstrument gbpjpy = new TradeableInstrument("GBP_JPY","GBP_JPY");
		Price gbpjpyPrice = new Price(gbpjpy, 166.506, 166.524, now);

		TradeableInstrument gbpnzd = new TradeableInstrument("GBP_NZD","GBP_NZD");
		Price gbpnzdPrice = new Price(gbpnzd, 2.17625, 2.17671, now);

		final double basePip = 45.0;
		PipJumpCutOffCalculator<String> pipCalculator = new PipJumpCutOffCalculatorService<>(eurusd,
				currentPriceInfoProvider, basePip, instrumentService);

		Map<TradeableInstrument, Price> eurusdnzdchfMap = Maps.newHashMap();
		eurusdnzdchfMap.put(eurusd, eurusdPrice);
		eurusdnzdchfMap.put(nzdchf, nzdchfPrice);

		Map<TradeableInstrument, Price> gbpnzdMap = Maps.newHashMap();
		gbpnzdMap.put(gbpnzd, gbpnzdPrice);

		Map<TradeableInstrument, Price> gbpjpyMap = Maps.newHashMap();
		gbpjpyMap.put(gbpjpy, gbpjpyPrice);

		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(eurusd, nzdchf))))
				.thenReturn(eurusdnzdchfMap);
		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(gbpnzd))))
				.thenReturn(gbpnzdMap);
		when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(gbpjpy))))
				.thenReturn(gbpjpyMap);

		when(instrumentService.getTickSizeForInstrument(eurusd)).thenReturn(pip1);
		when(instrumentService.getTickSizeForInstrument(gbpnzd)).thenReturn(pip1);
		when(instrumentService.getTickSizeForInstrument(gbpjpy)).thenReturn(pip2);
		when(instrumentService.getTickSizeForInstrument(nzdchf)).thenReturn(pip1);

		double v = pipCalculator.calculatePipJumpCutOff(nzdchf);
		assertEquals(26.2947, v, TradingTestConstants.PRECISION);

		v = pipCalculator.calculatePipJumpCutOff(gbpjpy);
		assertEquals(66.9571, v, TradingTestConstants.PRECISION);

		v = pipCalculator.calculatePipJumpCutOff(gbpnzd);
		assertEquals(87.5181, v, TradingTestConstants.PRECISION);

		v = pipCalculator.calculatePipJumpCutOff(nzdchf);
		v = pipCalculator.calculatePipJumpCutOff(gbpjpy);

		verify(currentPriceInfoProvider, times(3)).getCurrentPricesForInstruments(any(Collection.class));

	}
}
