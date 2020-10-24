package com.tradebot.bitmex.restapi.utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.http.HttpMethod;
import com.tradebot.bitmex.restapi.generated.restclient.ApiClient;
import com.tradebot.bitmex.restapi.generated.restclient.ApiException;
import com.tradebot.bitmex.restapi.generated.restclient.Pair;
import com.tradebot.bitmex.restapi.generated.restclient.ProgressRequestBody;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiClientAuthorizeable extends ApiClient {

    private static final long REQUEST_VALID = 5 * 60 * 60;

    private final String apiKey;
    private final String apiSecret;

    @Override
    public Request buildRequest(
        String path,
        String method,
        List<Pair> queryParams,
        List<Pair> collectionQueryParams,
        Object body,
        Map<String, String> headerParams,
        Map<String, Object> formParams,
        String[] authNames,
        ProgressRequestBody.ProgressRequestListener progressRequestListener
    ) throws ApiException {
        updateParamsForAuth(authNames, queryParams, headerParams);

        final String url = buildUrl(path, queryParams, collectionQueryParams);
        final Request.Builder reqBuilder = new Request.Builder().url(url);

        updateHeaders(headerParams, method, url, body);
        processHeaderParams(headerParams, reqBuilder);

        String contentType = headerParams.get("Content-Type");
        // ensuring a default content type
        if (contentType == null) {
            contentType = "application/json";
        }

        RequestBody reqBody;
        if (!HttpMethod.permitsRequestBody(method)) {
            reqBody = null;
        } else if ("application/x-www-form-urlencoded".equals(contentType)) {
            reqBody = buildRequestBodyFormEncoding(formParams);
        } else if ("multipart/form-data".equals(contentType)) {
            reqBody = buildRequestBodyMultipart(formParams);
        } else if (body == null) {
            if ("DELETE".equals(method)) {
                // allow calling DELETE without sending a request body
                reqBody = null;
            } else {
                // use an empty request body (for POST, PUT and PATCH)
                reqBody = RequestBody.create(MediaType.parse(contentType), "");
            }
        } else {
            reqBody = serialize(body, contentType);
        }

        Request request;

        if (progressRequestListener != null && reqBody != null) {
            ProgressRequestBody progressRequestBody = new ProgressRequestBody(reqBody, progressRequestListener);
            request = reqBuilder.method(method, progressRequestBody).build();
        } else {
            request = reqBuilder.method(method, reqBody).build();
        }

        if (log.isDebugEnabled()) {
            log.debug("Request: [{}], [{}]", request.toString(), request.body());
        }

        return request;
    }

    private void updateHeaders(Map<String, String> headerParams, String method, String url, Object body) throws ApiException {
        String expire = String.valueOf(Instant.now().getEpochSecond() + REQUEST_VALID);

        String fullPath;
        try {
            URL urlParser = new URL(url);
            fullPath = urlParser.getPath() + (urlParser.getQuery() != null ? "?" + urlParser.getQuery() : "");
        } catch (MalformedURLException e) {
            throw new ApiException(e);
        }

        String message = method + fullPath + expire + (body != null ? body.toString() : "");

        Key key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        HashCode hashCode = Hashing.hmacSha256(key).hashBytes(message.getBytes(StandardCharsets.UTF_8));

        headerParams.put("api-expires", expire);
        headerParams.put("api-key", apiKey);
        headerParams.put("api-signature", hashCode.toString());

    }


}
