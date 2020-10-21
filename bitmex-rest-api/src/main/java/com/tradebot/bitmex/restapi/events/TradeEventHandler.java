package com.tradebot.bitmex.restapi.events;

import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.transaction.Transaction;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.events.EventHandler;
import com.tradebot.core.events.EventPayLoad;
import com.tradebot.core.events.EventPayLoadToTweet;
import com.tradebot.core.events.notification.email.EmailContentGenerator;
import com.tradebot.core.events.notification.email.EmailPayLoad;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.trade.TradeInfoService;
import java.util.Set;

import org.json.simple.JSONObject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;

public class TradeEventHandler implements EventHandler<JSONObject, TradeEventPayLoad>,
    EmailContentGenerator<JSONObject>, EventPayLoadToTweet<JSONObject, TradeEventPayLoad> {

    private final Set<TradeEvents> tradeEventsSupported = Sets.newHashSet(TradeEvents.STOP_LOSS_FILLED,
            TradeEvents.TRADE_CLOSE, TradeEvents.TAKE_PROFIT_FILLED);
    private final TradeInfoService<Long, String, Long> tradeInfoService;
    private final TransactionDataProvider<Long, Long, String> transactionDataProvider;
    private final InstrumentService<String> instrumentService;

    public TradeEventHandler(TradeInfoService<Long, String, Long> tradeInfoService,
            TransactionDataProvider<Long, Long, String> transactionDataProvider,
            InstrumentService<String> instrumentService) {
        this.tradeInfoService = tradeInfoService;
        this.transactionDataProvider = transactionDataProvider;
        this.instrumentService = instrumentService;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleEvent(TradeEventPayLoad payLoad) {
        Preconditions.checkNotNull(payLoad);
        if (!tradeEventsSupported.contains(payLoad.getEvent())) {
            return;
        }
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        long accountId = (Long) jsonPayLoad.get(BitmexJsonKeys.accountId);
        tradeInfoService.refreshTradesForAccount(accountId);
    }

    @Override
    public EmailPayLoad generate(EventPayLoad<JSONObject> payLoad) {
        JSONObject jsonPayLoad = payLoad.getPayLoad();
        TradeableInstrument<String> instrument = new TradeableInstrument<>(jsonPayLoad.get(
                BitmexJsonKeys.instrument).toString());
        final String type = jsonPayLoad.get(BitmexJsonKeys.type).toString();
        final long accountId = (Long) jsonPayLoad.get(BitmexJsonKeys.accountId);
        final double accountBalance = ((Number) jsonPayLoad.get(BitmexJsonKeys.accountBalance)).doubleValue();
        final long tradeId = (Long) jsonPayLoad.get(BitmexJsonKeys.tradeId);
        final double pnl = ((Number) jsonPayLoad.get(BitmexJsonKeys.pl)).doubleValue();
        final double interest = ((Number) jsonPayLoad.get(BitmexJsonKeys.interest)).doubleValue();
        final long tradeUnits = (Long) jsonPayLoad.get(BitmexJsonKeys.units);
        final String emailMsg = String
                .format("Trade event %s received for account %d. Trade id=%d. Pnl=%5.3f, Interest=%5.3f, Trade Units=%d. Account balance after the event=%5.2f",
                        type, accountId, tradeId, pnl, interest, tradeUnits, accountBalance);
        final String subject = String.format("Trade event %s for %s", type, instrument.getInstrument());
        return new EmailPayLoad(subject, emailMsg);
    }

    @Override
    public String toTweet(TradeEventPayLoad payLoad) {
        if (!tradeEventsSupported.contains(payLoad.getEvent())) {
            return null;
        }

        final JSONObject jsonPayLoad = payLoad.getPayLoad();
        final String instrument =  jsonPayLoad.get(BitmexJsonKeys.instrument).toString();
        final String instrumentAsHashtag = BitmexUtils.bitmexToHashTagCcy(instrument);
        final long tradeUnits = (Long) jsonPayLoad.get(BitmexJsonKeys.units);
        final double price = ((Number) jsonPayLoad.get(BitmexJsonKeys.price)).doubleValue();

        final long origTransactionId = (Long) jsonPayLoad.get(BitmexJsonKeys.tradeId);
        final long accountId = (Long) jsonPayLoad.get(BitmexJsonKeys.accountId);
        Transaction<Long, Long, String> origTransaction = this.transactionDataProvider.getTransaction(
                origTransactionId,
                accountId);
        if (origTransaction == null) {
            String side = jsonPayLoad.get(BitmexJsonKeys.side).toString();
            TradingSignal signal = BitmexUtils.toTradingSignal(side);
            return String.format("Closed %s %d units of %s@%2.5f.", signal.flip().name(), tradeUnits,
                    instrumentAsHashtag, price);
        }

        double pips = 0;

        if (origTransaction.getSide() == TradingSignal.LONG) {
            pips = (price - origTransaction.getPrice())
                    / this.instrumentService.getPipForInstrument(new TradeableInstrument<>(instrument));
        } else {
            pips = (origTransaction.getPrice() - price)
                    / this.instrumentService.getPipForInstrument(new TradeableInstrument<>(instrument));
        }
        return String.format("Closed %s %d units of %s@%2.5f for %3.1f pips.", origTransaction.getSide().name(),
                tradeUnits,
                instrumentAsHashtag, price, pips);

    }

}
