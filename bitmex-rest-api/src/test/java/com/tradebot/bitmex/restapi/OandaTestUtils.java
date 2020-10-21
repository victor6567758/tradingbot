package com.tradebot.bitmex.restapi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class OandaTestUtils {

    private OandaTestUtils() {

    }

    public static final void mockHttpInteraction(String fname, HttpClient mockHttpClient)
        throws Exception {
        CloseableHttpResponse mockResp = mock(CloseableHttpResponse.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(mockResp);

        HttpEntity mockEntity = mock(HttpEntity.class);

        when(mockResp.getEntity()).thenReturn(mockEntity);

        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockResp.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(mockEntity.getContent()).thenReturn(new FileInputStream(fname));
    }
}
