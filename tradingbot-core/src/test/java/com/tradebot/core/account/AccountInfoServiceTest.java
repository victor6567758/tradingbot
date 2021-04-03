
package com.tradebot.core.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradebot.core.model.BaseTradingConfig;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.helper.ProviderHelper;
import com.tradebot.core.instrument.InstrumentDataProvider;
import com.tradebot.core.instrument.InstrumentPairInterestRate;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class AccountInfoServiceTest {

    private static final double MARGIN_RATE = 0.2;
    private static final int UNITS = 3000;

    private InstrumentDataProvider instrumentDataProvider;

    @Before
    public void init() {
        instrumentDataProvider = mock(InstrumentDataProvider.class);
        Collection<TradeableInstrument> instruments = createInstruments();

        when(instrumentDataProvider.getInstruments()).thenReturn(new OperationResultContext<>(instruments));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void accountsToTradeTest() {
        BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
        when(baseTradingConfig.getMinReserveRatio()).thenReturn(0.2);
        when(baseTradingConfig.getMinAmountRequired()).thenReturn(200.00);

        AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
        AccountInfoService<Long> accInfoService = new AccountInfoService<>(
            accountDataProvider,
            baseTradingConfig,
            operationResultContext -> {

            });
        List<Account<Long>> accounts = createAccounts();

        when(accountDataProvider.getLatestAccountsInfo()).thenReturn(new OperationResultContext<>(accounts));
        Collection<Long> eligibleAccounts = accInfoService.findAccountsToTrade();
        assertEquals(1, eligibleAccounts.size());

        long eligibleAccount = eligibleAccounts.iterator().next();
        assertEquals(1001L, eligibleAccount);
    }


    @SuppressWarnings("unchecked")
    private List<Account<Long>> createAccounts() {
        Account<Long> account1 = mock(Account.class);
        when(account1.getMarginAvailable()).thenReturn(new BigDecimal(1178.9));
        when(account1.getAmountAvailableRatio()).thenReturn(new BigDecimal(0.24));
        when(account1.getNetAssetValue()).thenReturn(new BigDecimal(1346.81));
        when(account1.getAccountId()).thenReturn(1001L);

        Account<Long> account2 = mock(Account.class);
        when(account2.getMarginAvailable()).thenReturn(new BigDecimal(100.23));
        when(account2.getAmountAvailableRatio()).thenReturn(new BigDecimal(0.04));
        when(account2.getNetAssetValue()).thenReturn(new BigDecimal(198.2));
        when(account2.getAccountId()).thenReturn(1002L);

        Account<Long> account3 = mock(Account.class);
        when(account3.getMarginAvailable()).thenReturn(new BigDecimal(1572.82));
        when(account3.getAmountAvailableRatio()).thenReturn(new BigDecimal(0.19));
        when(account3.getNetAssetValue()).thenReturn(new BigDecimal(2612.31));
        when(account3.getAccountId()).thenReturn(1003L);

        Account<Long> account4 = mock(Account.class);
        when(account4.getMarginAvailable()).thenReturn(new BigDecimal(198.34));
        when(account4.getAmountAvailableRatio()).thenReturn(new BigDecimal(0.45));
        when(account4.getNetAssetValue()).thenReturn(new BigDecimal(199.15));
        when(account4.getAccountId()).thenReturn(1004L);

        return Lists.newArrayList(account1, account2, account3, account4);
    }

    private Collection<TradeableInstrument> createInstruments() {
        Collection<TradeableInstrument> instruments = Lists.newArrayList();

        instruments.add(new TradeableInstrument("GBP_USD", "GBP_USD", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("GBP_CHF", "GBP_CHF", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("EUR_USD", "EUR_USD", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("NZD_USD", "NZD_USD", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("USD_JPY", "USD_JPY", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("AUD_JPY", "USD_JPY", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("AUD_USD", "AUD_USD", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("EUR_AUD", "EUR_AUD", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        instruments.add(new TradeableInstrument("AUD_EUR", "AUD_EUR", 0.001, mock(InstrumentPairInterestRate.class),
            StringUtils.EMPTY, null, null, null));
        return instruments;
    }

}
