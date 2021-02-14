package com.tradebot.core.account;

import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.helper.ProviderHelper;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountInfoService<K> {

    private final AccountDataProvider<K> accountDataProvider;
    private final BaseTradingConfig baseTradingConfig;
    private final CurrentPriceInfoProvider currentPriceInfoProvider;
    private final ProviderHelper<?> providerHelper;
    private final InstrumentService instrumentService;


    public AccountInfoService(AccountDataProvider<K> accountDataProvider,
        CurrentPriceInfoProvider currentPriceInfoProvider,
        BaseTradingConfig baseTradingConfig,
        ProviderHelper<?> providerHelper,
        InstrumentService instrumentService) {

        this.accountDataProvider = accountDataProvider;
        this.baseTradingConfig = baseTradingConfig;
        this.currentPriceInfoProvider = currentPriceInfoProvider;
        this.providerHelper = providerHelper;
        this.instrumentService = instrumentService;
    }

    public Collection<Account<K>> getAllAccounts() {
        return accountDataProvider.getLatestAccountsInfo();
    }

    public Account<K> getAccountInfo(K accountId) {
        return accountDataProvider.getLatestAccountInfo(accountId);
    }

    public Collection<K> findAccountsToTrade() {
        return getAllAccounts().stream()
            .sorted(Comparator.comparingDouble(account -> account.getMarginAvailable().doubleValue())).filter(
                account ->
                    account.getAmountAvailableRatio().doubleValue() >= baseTradingConfig.getMinReserveRatio()
                        && account.getNetAssetValue().doubleValue() >= baseTradingConfig.getMinAmountRequired()
            ).map(Account::getAccountId).collect(Collectors.toList());
    }

    public Optional<K> findAccountToTrade() {
        return getAllAccounts().stream()
            .sorted(Comparator.comparingDouble(account -> account.getMarginAvailable().doubleValue())).filter(
                account ->
                    account.getAmountAvailableRatio().doubleValue() >= baseTradingConfig.getMinReserveRatio()
                        && account.getNetAssetValue().doubleValue() >= baseTradingConfig.getMinAmountRequired()
            ).map(Account::getAccountId).findFirst();
    }

    /*
     * ({BASE} / {Home Currency}) * units) / (margin ratio)
    For example, suppose:
    Home Currency = USD
    Currency Pair = GBP/CHF
    Base = GBP; Quote = CHF
    Base / Home Currency = GBP/USD = 1.5819
    Units = 1000
    Margin Ratio = 20:1
    Then, margin used:
    = (1.5819 * 1000) / 20
    = 79.095 USD
     */
    @SuppressWarnings("unchecked")
    public double calculateMarginForTrade(Account<K> accountInfo, TradeableInstrument instrument,
        int units) {
        String[] tokens = TradingUtils.splitInstrumentPair(instrument.getInstrument());
        String baseCurrency = tokens[0];
        double price = 1.0;

        if (!baseCurrency.equals(accountInfo.getCurrency())) {
            String currencyPair = this.providerHelper
                .fromIsoFormat(baseCurrency + accountInfo.getCurrency());

            Map<TradeableInstrument, Price> priceInfoMap = this.currentPriceInfoProvider
                .getCurrentPricesForInstruments(
                    Collections.singletonList(instrumentService.resolveTradeableInstrument(currencyPair)));
            if (priceInfoMap.isEmpty()) {
                /*this means we got the currency pair inverted*/
                /*example when the home currency is GBP and instrument is USDJPY*/
                currencyPair = providerHelper.fromIsoFormat(accountInfo.getCurrency() + baseCurrency);
                priceInfoMap = currentPriceInfoProvider.getCurrentPricesForInstruments(
                    Collections.singletonList(instrumentService.resolveTradeableInstrument(currencyPair)));
                if (priceInfoMap.isEmpty()) {
                    // something else is wrong here
                    return Double.MAX_VALUE;
                }
                Price priceInfo = priceInfoMap.values().iterator().next();
                /*take avg of bid and ask prices*/
                price = 1.0 / ((priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0);
            } else {
                Price priceInfo = priceInfoMap.values().iterator().next();
                /*take avg of bid and ask prices*/
                price = (priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0;
            }

        }
        return price * units * accountInfo.getMarginRate().doubleValue();
    }

    public double calculateMarginForTrade(
        K accountId, TradeableInstrument instrument, int units) {
        return calculateMarginForTrade(getAccountInfo(accountId), instrument, units);
    }
}
