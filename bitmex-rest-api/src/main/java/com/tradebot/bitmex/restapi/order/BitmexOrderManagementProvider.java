
package com.tradebot.bitmex.restapi.order;

import static com.tradebot.bitmex.restapi.BitmexJsonKeys.expiry;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.id;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.instrument;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.orderOpened;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.orders;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.price;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.side;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.stopLoss;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.takeProfit;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.tradeOpened;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.type;
import static com.tradebot.bitmex.restapi.BitmexJsonKeys.units;

import com.google.common.collect.Lists;
import com.tradebot.bitmex.restapi.BitmexConstants;
import com.tradebot.bitmex.restapi.BitmexJsonKeys;
import com.tradebot.bitmex.restapi.utils.BitmexUtils;
import com.tradebot.core.TradingConstants;
import com.tradebot.core.TradingSignal;
import com.tradebot.core.account.Account;
import com.tradebot.core.account.AccountDataProvider;
import com.tradebot.core.instrument.TradeableInstrument;
import com.tradebot.core.order.Order;
import com.tradebot.core.order.OrderManagementProvider;
import com.tradebot.core.order.OrderType;
import com.tradebot.core.utils.TradingUtils;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class BitmexOrderManagementProvider implements OrderManagementProvider<Long, String, Long> {


    private final String url;
    private final BasicHeader authHeader;
    private static final String ordersResource = "/orders";
    private final AccountDataProvider<Long> accountDataProvider;

    public BitmexOrderManagementProvider(String url, String accessToken,
        AccountDataProvider<Long> accountDataProvider) {
        this.url = url;
        this.authHeader = BitmexUtils.createAuthHeader(accessToken);
        this.accountDataProvider = accountDataProvider;
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    @Override
    public boolean closeOrder(Long orderId, Long accountId) {

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpDelete httpDelete = new HttpDelete(orderForAccountUrl(accountId, orderId));
            httpDelete.setHeader(authHeader);
            log.info(TradingUtils.executingRequestMsg(httpDelete));
            HttpResponse resp = httpClient.execute(httpDelete);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.info(String
                    .format("Order %d successfully deleted for account %d", orderId, accountId));
                return true;
            } else {
                log.warn(
                    String.format("Order %d could not be deleted. Recd error code %d", orderId, resp
                        .getStatusLine().getStatusCode()));
            }
        } catch (Exception e) {
            log.warn("error deleting order id:" + orderId, e);
        }
        return false;
    }

    HttpPatch createPatchCommand(Order<String, Long> order, Long accountId) throws Exception {
        HttpPatch httpPatch = new HttpPatch(orderForAccountUrl(accountId, order.getOrderId()));
        httpPatch.setHeader(this.authHeader);
        List<NameValuePair> params = Lists.newArrayList();
        params.add(new BasicNameValuePair(takeProfit, String.valueOf(order.getTakeProfit())));
        params.add(new BasicNameValuePair(stopLoss, String.valueOf(order.getStopLoss())));
        params.add(new BasicNameValuePair(units, String.valueOf(order.getUnits())));
        params.add(new BasicNameValuePair(price, String.valueOf(order.getPrice())));
        httpPatch.setEntity(new UrlEncodedFormEntity(params));
        return httpPatch;
    }

    HttpPost createPostCommand(Order<String, Long> order, Long accountId) throws Exception {
        HttpPost httpPost = new HttpPost(
            this.url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH
                + accountId + ordersResource);
        httpPost.setHeader(this.authHeader);
        List<NameValuePair> params = Lists.newArrayList();
        // TODO: apply proper rounding. Oanda rejects 0.960000001
        params.add(new BasicNameValuePair(instrument, order.getInstrument().getInstrument()));
        params.add(new BasicNameValuePair(side, BitmexUtils.toSide(order.getSide())));
        params.add(new BasicNameValuePair(type, BitmexUtils.toType(order.getType())));
        params.add(new BasicNameValuePair(units, String.valueOf(order.getUnits())));
        params.add(new BasicNameValuePair(takeProfit, String.valueOf(order.getTakeProfit())));
        params.add(new BasicNameValuePair(stopLoss, String.valueOf(order.getStopLoss())));

        // TODO: why this code
        // for expiry?
        if (order.getType() == OrderType.LIMIT && order.getPrice() != 0.0) {
            DateTime now = DateTime.now();
            DateTime nowplus4hrs = now.plusHours(4);
            String dateStr = nowplus4hrs.toString();
            params.add(new BasicNameValuePair(price, String.valueOf(order.getPrice())));
            params.add(new BasicNameValuePair(expiry, dateStr));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        return httpPost;
    }

    @Override
    public Long placeOrder(Order<String, Long> order, Long accountId) {

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPost httpPost = createPostCommand(order, accountId);
            log.info(TradingUtils.executingRequestMsg(httpPost));
            HttpResponse resp = httpClient.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                || resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                if (resp.getEntity() != null) {
                    String strResp = TradingUtils.responseToString(resp);
                    Object o = JSONValue.parse(strResp);
                    JSONObject orderResponse;
                    if (order.getType() == OrderType.MARKET) {
                        orderResponse = (JSONObject) ((JSONObject) o).get(tradeOpened);
                    } else {
                        orderResponse = (JSONObject) ((JSONObject) o).get(orderOpened);
                    }
                    Long orderId = (Long) orderResponse.get(BitmexJsonKeys.id);
                    log.info("Order executed->" + strResp);
                    return orderId;
                } else {
                    return null;
                }

            } else {
                log.info(String.format("Order not executed. http code=%d. Order pojo->%s",
                    resp.getStatusLine().getStatusCode(), order.toString()));
                return null;
            }
        } catch (Exception e) {
            log.warn("order warning", e);
            return null;
        }
    }

    @Override
    public Order<String, Long> pendingOrderForAccount(Long orderId, Long accountId) {

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(orderForAccountUrl(accountId, orderId));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);
            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (!StringUtils.isEmpty(strResp)) {
                JSONObject order = (JSONObject) JSONValue.parse(strResp);
                return parseOrder(order);
            } else {
                log.error("Error: {}", resp);
            }
        } catch (Exception e) {
            log.error(String.format(
                "error encountered whilst fetching pending order for account %d and order id %d",
                accountId, orderId), e);
        }
        return null;
    }

    private Order<String, Long> parseOrder(JSONObject order) {
        final String orderInstrument = (String) order.get(instrument);
        final Long orderUnits = (Long) order.get(units);
        final TradingSignal orderSide = BitmexUtils.toTradingSignal((String) order.get(side));
        final OrderType orderType = BitmexUtils.toOrderType((String) order.get(type));
        final double orderTakeProfit = ((Number) order.get(takeProfit)).doubleValue();
        final double orderStopLoss = ((Number) order.get(stopLoss)).doubleValue();
        final double orderPrice = ((Number) order.get(price)).doubleValue();
        final Long orderId = (Long) order.get(id);
        Order<String, Long> pendingOrder = new Order<>(
            new TradeableInstrument<>(orderInstrument),
            orderUnits, orderSide, orderType, orderTakeProfit, orderStopLoss, orderPrice);
        pendingOrder.setOrderId(orderId);
        return pendingOrder;
    }

    @Override
    public Collection<Order<String, Long>> pendingOrdersForInstrument(
        TradeableInstrument<String> instrument) {
        Collection<Account<Long>> accounts = this.accountDataProvider.getLatestAccountsInfo();
        Collection<Order<String, Long>> allOrders = Lists.newArrayList();
        for (Account<Long> account : accounts) {
            allOrders.addAll(this.pendingOrdersForAccount(account.getAccountId(), instrument));
        }
        return allOrders;
    }

    @Override
    public Collection<Order<String, Long>> allPendingOrders() {
        return pendingOrdersForInstrument(null);
    }

    private Collection<Order<String, Long>> pendingOrdersForAccount(Long accountId,
        TradeableInstrument<String> instrument) {
        Collection<Order<String, Long>> pendingOrders = Lists.newArrayList();

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpUriRequest httpGet = new HttpGet(this.url + BitmexConstants.ACCOUNTS_RESOURCE
                + TradingConstants.FWD_SLASH + accountId + ordersResource
                + (instrument != null ? "?instrument=" + instrument.getInstrument()
                : StringUtils.EMPTY));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BitmexConstants.UNIX_DATETIME_HEADER);
            log.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (!StringUtils.isEmpty(strResp)) {
                Object obj = JSONValue.parse(strResp);
                JSONObject jsonResp = (JSONObject) obj;
                JSONArray accountOrders = (JSONArray) jsonResp.get(orders);

                for (Object o : accountOrders) {
                    JSONObject order = (JSONObject) o;
                    Order<String, Long> pendingOrder = parseOrder(order);
                    pendingOrders.add(pendingOrder);
                }
            } else {
                log.error("Error: {}", resp);
            }
        } catch (Exception e) {
            log.error(String.format(
                "error encountered whilst fetching pending orders for account %d and instrument %s",
                accountId, instrument.getInstrument()), e);
        }
        return pendingOrders;
    }

    @Override
    public Collection<Order<String, Long>> pendingOrdersForAccount(Long accountId) {
        return this.pendingOrdersForAccount(accountId, null);
    }

    String orderForAccountUrl(Long accountId, Long orderId) {
        return this.url + BitmexConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
            + ordersResource
            + TradingConstants.FWD_SLASH + orderId;
    }

    @Override
    public boolean modifyOrder(Order<String, Long> order, Long accountId) {

        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPatch httpPatch = createPatchCommand(order, accountId);
            log.info(TradingUtils.executingRequestMsg(httpPatch));
            HttpResponse resp = httpClient.execute(httpPatch);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                && resp.getEntity() != null) {
                log.info("Order Modified->" + TradingUtils.responseToString(resp));
                return true;
            }
            log.warn(String.format("order %s could not be modified.", order.toString()));
        } catch (Exception e) {
            log.error(String
                .format("error encountered whilst modifying order %d for account %d", order,
                    accountId), e);
        }
        return false;
    }

}
