package com.tradebot.bitmex.restapi.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.tradebot.bitmex.restapi.utils.converters.OrderTypeConvertible;
import com.tradebot.bitmex.restapi.utils.converters.TradingSignalConvertible;
import com.tradebot.core.model.ExecutionType;
import com.tradebot.core.model.TradingSignal;
import com.tradebot.core.order.OrderStatus;
import com.tradebot.core.order.OrderType;
import lombok.experimental.UtilityClass;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@UtilityClass
public class BitmexJsonBuilder {

    public static Gson buildJson() {
        return new GsonBuilder()
            .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc, context) ->
                new JsonPrimitive(ISODateTimeFormat.dateTime().print(json)))
            .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) ->
                ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()))
            //
            .registerTypeAdapter(TradingSignal.class, (JsonDeserializer<TradingSignal>) (json, typeOfT, context) ->
                TradingSignalConvertible.fromString(json.getAsString()))
            .registerTypeAdapter(TradingSignal.class, (JsonSerializer<TradingSignal>) (src, typeOfSrc, context) ->
                new JsonPrimitive(TradingSignalConvertible.toString(src)))
            //
            .registerTypeAdapter(OrderType.class, (JsonDeserializer<OrderType>) (json, typeOfT, context) ->
                OrderTypeConvertible.fromString(json.getAsString()))
            .registerTypeAdapter(OrderType.class, (JsonSerializer<OrderType>) (src, typeOfSrc, context) ->
                new JsonPrimitive(OrderTypeConvertible.toString(src)))
            //
            .registerTypeAdapter(OrderStatus.class, (JsonDeserializer<OrderStatus>) (json, typeOfT, context) ->
                BitmexUtils.findByStringMarker(OrderStatus.values(), orderStatus ->
                    orderStatus.getStatusText().equals(json.getAsString())))
            .registerTypeAdapter(OrderStatus.class, (JsonSerializer<OrderStatus>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.getStatusText()))

            //
            .registerTypeAdapter(ExecutionType.class, (JsonDeserializer<ExecutionType>) (json, typeOfT, context) ->
                BitmexUtils.findByStringMarker(ExecutionType.values(), executionType ->
                    executionType.getExecutionTypeText().equals(json.getAsString())))
            .registerTypeAdapter(ExecutionType.class, (JsonSerializer<ExecutionType>) (src, typeOfSrc, context) ->
                new JsonPrimitive(src.getExecutionTypeText()))
            //
            .create();
    }
}
