package org.web.remoting;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.Arrays;

public class WebClient {

    public Object call(String url, HttpEntity payload){
        // default client builder
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        // configured client instance
        try (CloseableHttpClient httpClient = clientBuilder.build()){
            // get request
            ClassicHttpRequest classicHttpRequest = ClassicRequestBuilder
                    //.get("http://localhost:9080/sit/connect.do?confirmer=true")
                    .get("http://httpbin.org/get")
                    .setHeader(HttpHeaders.ACCEPT, "text/plain")
                    .build();
            httpClient.execute(classicHttpRequest, response -> {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                final HttpEntity responseEntity = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(responseEntity);
                return null;
            });

            // post request
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post("http://httpbin.org/post")
                    .setEntity(new UrlEncodedFormEntity(Arrays.asList(
                            new BasicNameValuePair("username", "vip"),
                            new BasicNameValuePair("password", "secret"))))
                    .build();
            httpClient.execute(httpPost, response -> {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                final HttpEntity responseEntity = response.getEntity();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(responseEntity);
                return null;
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; // TODO
    }
}
