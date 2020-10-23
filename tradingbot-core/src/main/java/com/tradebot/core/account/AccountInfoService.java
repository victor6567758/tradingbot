package com.tradebot.core.account;

import com.google.common.collect.Lists;
import com.tradebot.core.BaseTradingConfig;
import com.tradebot.core.helper.ProviderHelper;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.marketdata.CurrentPriceInfoProvider;
import com.tradebot.core.marketdata.Price;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountInfoService<K, N> {

    private final AccountDataProvider<K> accountDataProvider;
    private final BaseTradingConfig baseTradingConfig;
    private final CurrentPriceInfoProvider<N> currentPriceInfoProvider;
    private final ProviderHelper<?> providerHelper;


    public AccountInfoService(AccountDataProvider<K> accountDataProvider,
        CurrentPriceInfoProvider<N> currentPriceInfoProvider,
        BaseTradingConfig baseTradingConfig,
        ProviderHelper<?> providerHelper) {

        this.accountDataProvider = accountDataProvider;
        this.baseTradingConfig = baseTradingConfig;
        this.currentPriceInfoProvider = currentPriceInfoProvider;
        this.providerHelper = providerHelper;
    }

    public Collection<Account<K>> getAllAccounts() {
        return accountDataProvider.getLatestAccountsInfo();
    }

    public Account<K> getAccountInfo(K accountId) {
        return accountDataProvider.getLatestAccountInfo(accountId);
    }

    public Collection<K> findAccountsToTrade() {
        return getAllAccounts().stream()
            .sorted(Comparator.comparingDouble(Account::getMarginAvailable)).filter(
                account ->
                    account.getAmountAvailableRatio() >= baseTradingConfig.getMinReserveRatio()
                        && account.getNetAssetValue() >= baseTradingConfig.getMinAmountRequired()
            ).map(Account::getAccountId).collect(Collectors.toList());
    }

    public Optional<K> findAccountToTrade() {
        return getAllAccounts().stream()
            .sorted(Comparator.comparingDouble(Account::getMarginAvailable)).filter(
                account ->
                    account.getAmountAvailableRatio() >= baseTradingConfig.getMinReserveRatio()
                        && account.getNetAssetValue() >= baseTradingConfig.getMinAmountRequired()
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
    public double calculateMarginForTrade(Account<K> accountInfo, TradeableInstrument<N> instrument,
        int units) {
        String[] tokens = TradingUtils.splitInstrumentPair(instrument.getInstrument());
        String baseCurrency = tokens[0];
        double price = 1.0;

        if (!baseCurrency.equals(accountInfo.getCurrency())) {
            String currencyPair = this.providerHelper
                .fromIsoFormat(baseCurrency + accountInfo.getCurrency());

            Map<TradeableInstrument<N>, Price<N>> priceInfoMap = this.currentPriceInfoProvider
                .getCurrentPricesForInstruments(
                    Lists.newArrayList(new TradeableInstrument<N>(currencyPair)));
            if (priceInfoMap.isEmpty()) {
                /*this means we got the currency pair inverted*/
                /*example when the home currency is GBP and instrument is USDJPY*/
                currencyPair = providerHelper
                    .fromIsoFormat(accountInfo.getCurrency() + baseCurrency);
                priceInfoMap = currentPriceInfoProvider.getCurrentPricesForInstruments(
                    Lists.newArrayList(new TradeableInstrument<N>(currencyPair)));
                if (priceInfoMap.isEmpty()) {
                    // something else is wrong here
                    return Double.MAX_VALUE;
                }
                Price<N> priceInfo = priceInfoMap.values().iterator().next();
                /*take avg of bid and ask prices*/
                price = 1.0 / ((priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0);
            } else {
                Price<N> priceInfo = priceInfoMap.values().iterator().next();
                /*take avg of bid and ask prices*/
                price = (priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0;
            }

        }
        return price * units * accountInfo.getMarginRate();
    }

    public double calculateMarginForTrade(
        K accountId, TradeableInstrument<N> instrument, int units) {
        return calculateMarginForTrade(getAccountInfo(accountId), instrument, units);
    }
}
