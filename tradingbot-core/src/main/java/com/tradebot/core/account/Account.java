package com.tradebot.core.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;

@Getter
public class Account<T> {

    private final BigDecimal totalBalance;
    private final BigDecimal unrealisedPnl;
    private final BigDecimal realisedPnl;
    private final BigDecimal marginUsed;
    /*The leverage offered on this account. for e.g. 0.05, 0.1 etc*/
    private final BigDecimal marginRate;
    private final BigDecimal marginAvailable;
    private final long openTrades;
    private final String currency;
    private final T accountId;
    private final String toStr;

    /*The amount available to trade as a fraction of total amount*/
    private final BigDecimal amountAvailableRatio;
    private final BigDecimal netAssetValue;

    private final int hash;


    public Account(
        BigDecimal totalBalance,
        BigDecimal unrealisedPnl,
        BigDecimal realisedPnl,
        BigDecimal marginUsed,
        BigDecimal marginAvailable,
        long openTrades,
        String currency,
        T accountId,
        BigDecimal marginRate) {

        this.totalBalance = totalBalance;
        this.unrealisedPnl = unrealisedPnl;
        this.realisedPnl = realisedPnl;
        this.marginUsed = marginUsed;
        this.marginAvailable = marginAvailable;
        this.openTrades = openTrades;
        this.currency = currency;
        this.accountId = accountId;
        this.marginRate = marginRate;

        this.amountAvailableRatio = this.marginAvailable.divide(this.totalBalance, 3, RoundingMode.HALF_UP);
        this.netAssetValue = this.marginUsed.add(this.marginAvailable);


        this.hash = calcHashCode();
        toStr = String.format("Currency=%s,NAV=%5.2f,Total Balance=%5.2f, UnrealisedPnl=%5.2f, "
                + "RealisedPnl=%5.2f, MarginUsed=%5.2f, MarginAvailable=%5.2f,"
                + " OpenTrades=%d, amountAvailableRatio=%1.2f, marginRate=%1.2f",
            currency,
            netAssetValue,
            totalBalance,
            unrealisedPnl,
            realisedPnl,
            marginUsed,
            marginAvailable,
            openTrades,
            this.amountAvailableRatio,
            this.marginRate);
    }


    @Override
    public String toString() {
        return this.toStr;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Account<T> other = (Account<T>) obj;
        if (accountId == null) {
            return other.accountId == null;
        } else {
            return accountId.equals(other.accountId);
        }
    }

    private int calcHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        return result;
    }

}
