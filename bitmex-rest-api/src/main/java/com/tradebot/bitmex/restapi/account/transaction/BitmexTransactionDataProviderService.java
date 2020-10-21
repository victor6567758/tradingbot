package com.tradebot.bitmex.restapi.account.transaction;

import static java.math.BigDecimal.ZERO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.events.AccountEvents;
import com.tradebot.bitmex.restapi.events.OrderEvents;
import com.tradebot.bitmex.restapi.events.TradeEvents;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.account.transaction.Transaction;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.events.Event;
import com.tradebot.core.utils.TradingUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class BitmexTransactionDataProviderService implements
    TransactionDataProvider<Long, Long, String> {

    private final String url;
    private final BasicHeader authHeader;
    private static final String TRANSACTIONS = "transactions";

    public BitmexTransactionDataProviderService(final String url, final String accessToken) {
        this.url = url;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    String getSingleAccountTransactionUrl(Long transactionId, Long accountId) {
        return url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + TradingConstants.FWD_SLASH
            + TRANSACTIONS + TradingConstants.FWD_SLASH + transactionId;
    }

    /*
     * only 50 would be returned, refer
     * documentation
     */
    String getAccountMinTransactionUrl(Long minTransactionId,
        Long accountId) {
        return url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + TradingConstants.FWD_SLASH
            + TRANSACTIONS + "?minId=" + (minTransactionId + 1) + "&count=500";
    }

    @Override
    public Transaction<Long, Long, String> getTransaction(Long transactionId, Long accountId) {
        Preconditions.checkNotNull(transactionId);
        Preconditions.checkNotNull(accountId);

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(
                getSingleAccountTransactionUrl(transactionId, accountId));
            httpGet.setHeader(authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);

            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(httpResponse);
            if (strResp != StringUtils.EMPTY) {
                JSONObject transactionJson = (JSONObject) JSONValue.parse(strResp);
                /* do not use derive instrument here */
                final String instrument = transactionJson.get(BitmexJsonKeys.instrument)
                    .toString();
                final String type = transactionJson.get(BitmexJsonKeys.type).toString();
                final Long tradeUnits = getUnits(transactionJson);
                final DateTime timestamp = getTransactionTime(transactionJson);
                final Double pnl = getPnl(transactionJson);
                final Double interest = getInterest(transactionJson);
                final Double price = getPrice(transactionJson);
                final String side = getSide(transactionJson);
                return new Transaction<Long, Long, String>(transactionId, BitmexUtils.toBitmexTransactionType(type),
                    accountId, instrument, tradeUnits, BitmexUtils.toTradingSignal(side), timestamp, price, interest,
                    pnl);
            } else {
                log.error("Error: {}", httpResponse);
            }
        } catch (Exception e) {
            log.error("Error", e);
        }
        return null;
    }

    @Override
    public List<Transaction<Long, Long, String>> getTransactionsGreaterThanId(Long minTransactionId,
        Long accountId) {
        Preconditions.checkNotNull(minTransactionId);
        Preconditions.checkNotNull(accountId);

        List<Transaction<Long, Long, String>> allTransactions = Lists.newArrayList();
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(
                getAccountMinTransactionUrl(minTransactionId, accountId));
            httpGet.setHeader(authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);

            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(httpResponse);

            if (strResp != StringUtils.EMPTY) {
                Object obj = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) obj;
                JSONArray transactionsJson = (JSONArray) jsonResp.get(TRANSACTIONS);
                for (Object o : transactionsJson) {
                    try {
                        JSONObject transactionJson = (JSONObject) o;
                        final String type = transactionJson.get(BitmexJsonKeys.type).toString();
                        Event transactionEvent = BitmexUtils.toBitmexTransactionType(type);
                        Transaction<Long, Long, String> transaction = null;
                        if (transactionEvent instanceof TradeEvents) {
                            transaction = handleTradeTransactionEvent(
                                (TradeEvents) transactionEvent, transactionJson);
                        } else if (transactionEvent instanceof AccountEvents) {
                            transaction = handleAccountTransactionEvent(
                                (AccountEvents) transactionEvent,
                                transactionJson);
                        } else if (transactionEvent instanceof OrderEvents) {
                            transaction = handleOrderTransactionEvent(
                                (OrderEvents) transactionEvent, transactionJson);
                        }

                        if (transaction != null) {
                            allTransactions.add(transaction);
                        }
                    } catch (Exception e) {
                        log.error("error encountered whilst parsing:" + o, e);
                    }

                }
            } else {
                log.error("Error: {}", httpResponse);
            }
        } catch (Exception e) {
            log.error("error encountered->", e);
        }
        return allTransactions;
    }

    private String deriveInstrument(JSONObject transactionJson) {
        String strInstr = transactionJson.get(BitmexJsonKeys.instrument).toString();
        return strInstr;
    }

    private Transaction<Long, Long, String> handleTradeTransactionEvent(TradeEvents tradeEvent,
        JSONObject transactionJson) {
        Transaction<Long, Long, String> transaction = null;
        String strInstr = deriveInstrument(transactionJson);
        switch (tradeEvent) {
            case TAKE_PROFIT_FILLED:
            case STOP_LOSS_FILLED:
            case TRADE_CLOSE:
            case TRAILING_STOP_FILLED:
                transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson),
                    tradeEvent,
                    getAccountId(transactionJson), strInstr, getUnits(transactionJson),
                    BitmexUtils.toTradingSignal(getSide(transactionJson)),
                    getTransactionTime(transactionJson),
                    getPrice(transactionJson), getInterest(transactionJson),
                    getPnl(transactionJson));
                transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
                return transaction;
            case TRADE_UPDATE:
                transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson),
                    tradeEvent,
                    getAccountId(transactionJson), strInstr, getUnits(transactionJson), null,
                    getTransactionTime(transactionJson), null, null, null);
                transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
                return transaction;
            default:
                break;
        }
        return transaction;
    }

    private Transaction<Long, Long, String> handleAccountTransactionEvent(
        AccountEvents accountEvent,
        JSONObject transactionJson) {
        Transaction<Long, Long, String> transaction = null;
        String strInstr = deriveInstrument(transactionJson);
        switch (accountEvent) {
            case DAILY_INTEREST:
                transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson),
                    accountEvent,
                    getAccountId(transactionJson), strInstr, null, null,
                    getTransactionTime(transactionJson), null,
                    getInterest(transactionJson), null);
                transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
                return transaction;
            default:
                break;
        }
        return transaction;
    }

    private Transaction<Long, Long, String> handleOrderTransactionEvent(OrderEvents orderEvent,
        JSONObject transactionJson) {
        Transaction<Long, Long, String> transaction = null;
        String strInstr = null;
        switch (orderEvent) {
            case LIMIT_ORDER_CREATE:
                strInstr = deriveInstrument(transactionJson);
                transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson),
                    orderEvent,
                    getAccountId(transactionJson), strInstr, getUnits(transactionJson),
                    BitmexUtils.toTradingSignal(getSide(transactionJson)),
                    getTransactionTime(transactionJson),
                    getPrice(transactionJson), null, null);
                transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
                break;
            case MARKET_ORDER_CREATE:
            case ORDER_FILLED:
                strInstr = deriveInstrument(transactionJson);
                transaction = new Transaction<Long, Long, String>(getTransactionId(transactionJson),
                    orderEvent,
                    getAccountId(transactionJson), strInstr, getUnits(transactionJson),
                    BitmexUtils.toTradingSignal(getSide(transactionJson)),
                    getTransactionTime(transactionJson),
                    getPrice(transactionJson), getInterest(transactionJson),
                    getPnl(transactionJson));
                transaction.setLinkedTransactionId(getLinkedTradeId(transactionJson));
                break;
            case ORDER_CANCEL:
            case ORDER_UPDATE:
            default:
                break;
        }
        return transaction;
    }

    private String getSide(JSONObject transaction) {
        return transaction.get(BitmexJsonKeys.side).toString();
    }

    private Double getPrice(JSONObject transaction) {
        return ((Number) transaction.get(BitmexJsonKeys.price)).doubleValue();
    }

    private Long getUnits(JSONObject transaction) {
        return (Long) transaction.get(BitmexJsonKeys.units);
    }

    private Double getInterest(JSONObject transaction) {
        return ((Number) transaction.get(BitmexJsonKeys.interest)).doubleValue();
    }

    private Double getPnl(JSONObject transaction) {
        return ((Number) transaction.get(BitmexJsonKeys.pl)).doubleValue();
    }

    private DateTime getTransactionTime(JSONObject transaction) {
        Long transactionTime = Long.parseLong(transaction.get(BitmexJsonKeys.time).toString());
        return new DateTime(TradingUtils.toMillisFromNanos(transactionTime));
    }

    private Long getAccountId(JSONObject transaction) {
        return (Long) transaction.get(BitmexJsonKeys.accountId);
    }

    private Long getTransactionId(JSONObject transaction) {
        return (Long) transaction.get(BitmexJsonKeys.id);
    }

    private Long getLinkedTradeId(JSONObject transaction) {
        Long lnkedTransactionId = (Long) transaction.get(BitmexJsonKeys.tradeId);
        if (lnkedTransactionId == null) {
            lnkedTransactionId = ZERO.longValue();
        }
        return lnkedTransactionId;
    }

}
