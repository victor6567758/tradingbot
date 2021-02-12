package com.tradebot.core.order;

public abstract class OrderExecutionServiceContext {

    protected long fireTime = -1;

    public void fired() {
        fireTime = System.currentTimeMillis();
    }

    public abstract boolean ifTradeAllowed();

    public abstract String getReason();


}
