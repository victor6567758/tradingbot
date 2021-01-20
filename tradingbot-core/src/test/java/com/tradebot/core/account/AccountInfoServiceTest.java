
package com.tradebot.core.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.TradingTestConstants;
import com.tradebot.core.helper.ProviderHelper;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.Test;

public class AccountInfoServiceTest {

    private static final double MARGIN_RATE = 0.2;
    private static final int UNITS = 3000;

    @Test
    @SuppressWarnings("unchecked")
    public void accountsToTradeTest() {
        BaseTradingConfig baseTradingConfig = mock(BaseTradingConfig.class);
        when(baseTradingConfig.getMinReserveRatio()).thenReturn(0.2);
        when(baseTradingConfig.getMinAmountRequired()).thenReturn(200.00);

        AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
        AccountInfoService<Long> accInfoService = new AccountInfoService<>(accountDataProvider,
            null, baseTradingConfig, null);
        List<Account<Long>> accounts = createAccounts();

        when(accountDataProvider.getLatestAccountsInfo()).thenReturn(accounts);
        Collection<Long> eligibleAccounts = accInfoService.findAccountsToTrade();
        assertEquals(1, eligibleAccounts.size());

        long eligibleAccount = eligibleAccounts.iterator().next();
        assertEquals(1001L, eligibleAccount);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void marginRateWhenAccountCurrencyNominatedTest() {
        /*account currency CHF and calculate margin for GBPUSD, effectively using GBPCHF rate*/
        AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
        CurrentPriceInfoProvider currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
        ProviderHelper<?> providerHelper = mock(ProviderHelper.class);
        AccountInfoService<Long> accInfoService = new AccountInfoService<>(accountDataProvider,
            currentPriceInfoProvider, null, providerHelper);
        TradeableInstrument gbpusd = new TradeableInstrument("GBP_USD","GBP_USD");
        TradeableInstrument gbpchf = new TradeableInstrument("GBP_CHF","GBP_CHF");
        Account<Long> account = mock(Account.class);

        when(accountDataProvider.getLatestAccountInfo(TradingTestConstants.ACCOUNT_ID_1)).thenReturn(account);
        when(account.getCurrency()).thenReturn("CHF");
        when(account.getMarginRate()).thenReturn(MARGIN_RATE);
        when(providerHelper.fromIsoFormat(eq("GBPCHF"))).thenReturn(gbpchf.getInstrument());

        Map<TradeableInstrument, Price> priceInfoMap = Maps.newHashMap();
        priceInfoMap.put(gbpchf, new Price(gbpchf, 1.4811, 1.4813, DateTime.now()));
        when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(gbpchf)))).thenReturn(
            priceInfoMap);

        double marginRate = accInfoService.calculateMarginForTrade(TradingTestConstants.ACCOUNT_ID_1, gbpusd,
            UNITS);
        assertEquals(888.72, marginRate, TradingTestConstants.PRECISION);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void marginRateWhenAccountCurrencyBaseTest() {
        /*account currency EUR and calculate margin for AUDUSD, effectively using AUDEUR rate*/
        AccountDataProvider<Long> accountDataProvider = mock(AccountDataProvider.class);
        CurrentPriceInfoProvider currentPriceInfoProvider = mock(CurrentPriceInfoProvider.class);
        ProviderHelper<?> providerHelper = mock(ProviderHelper.class);
        AccountInfoService<Long> accInfoService = new AccountInfoService<>(accountDataProvider,
            currentPriceInfoProvider, null, providerHelper);
        TradeableInstrument audusd = new TradeableInstrument("AUD_USD","AUD_USD");
        TradeableInstrument euraud = new TradeableInstrument("EUR_AUD", "EUR_AUD");
        TradeableInstrument audeur = new TradeableInstrument("AUD_EUR", "AUD_EUR");
        Account<Long> account = mock(Account.class);

        when(accountDataProvider.getLatestAccountInfo(TradingTestConstants.ACCOUNT_ID_1)).thenReturn(account);
        when(account.getCurrency()).thenReturn("EUR");
        when(account.getMarginRate()).thenReturn(MARGIN_RATE);
        when(providerHelper.fromIsoFormat(eq("AUDEUR"))).thenReturn(audeur.getInstrument());
        when(providerHelper.fromIsoFormat(eq("EURAUD"))).thenReturn(euraud.getInstrument());

        Map<TradeableInstrument, Price> priceInfoMap = Maps.newHashMap();
        priceInfoMap.put(euraud, new Price(euraud, 1.5636, 1.564, DateTime.now()));
        when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(audeur)))).thenReturn(
            Maps.newHashMap());
        when(currentPriceInfoProvider.getCurrentPricesForInstruments(eq(Lists.newArrayList(euraud)))).thenReturn(
            priceInfoMap);

        double marginRate = accInfoService.calculateMarginForTrade(TradingTestConstants.ACCOUNT_ID_1, audusd,
            UNITS);
        assertEquals(383.6807, marginRate, TradingTestConstants.PRECISION);
        marginRate = accInfoService.calculateMarginForTrade(TradingTestConstants.ACCOUNT_ID_1, euraud,
            UNITS);
        assertEquals(600.0, marginRate, TradingTestConstants.PRECISION);
    }

    @SuppressWarnings("unchecked")
    private List<Account<Long>> createAccounts() {
        Account<Long> account1 = mock(Account.class);
        when(account1.getMarginAvailable()).thenReturn(1178.9);
        when(account1.getAmountAvailableRatio()).thenReturn(0.24);
        when(account1.getNetAssetValue()).thenReturn(1346.81);
        when(account1.getAccountId()).thenReturn(1001L);

        Account<Long> account2 = mock(Account.class);
        when(account2.getMarginAvailable()).thenReturn(100.23);
        when(account2.getAmountAvailableRatio()).thenReturn(0.04);
        when(account2.getNetAssetValue()).thenReturn(198.2);
        when(account2.getAccountId()).thenReturn(1002L);

        Account<Long> account3 = mock(Account.class);
        when(account3.getMarginAvailable()).thenReturn(1572.82);
        when(account3.getAmountAvailableRatio()).thenReturn(0.19);
        when(account3.getNetAssetValue()).thenReturn(2612.31);
        when(account3.getAccountId()).thenReturn(1003L);

        Account<Long> account4 = mock(Account.class);
        when(account4.getMarginAvailable()).thenReturn(198.34);
        when(account4.getAmountAvailableRatio()).thenReturn(0.45);
        when(account4.getNetAssetValue()).thenReturn(199.15);
        when(account4.getAccountId()).thenReturn(1004L);

        return Lists.newArrayList(account1, account2, account3, account4);
    }


}
