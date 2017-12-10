/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.barracks.googleanalyticsservice.client;

import io.barracks.commons.util.Endpoint;
import io.barracks.googleanalyticsservice.client.exception.GoogleAnalyticsClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleAnalyticsClient {

    static final Endpoint GOOGLE_ANALYTICS_ENDPOINT = Endpoint.from(HttpMethod.POST, "/collect",
            "v={v}&t={t}&ec={key}&ea={value}&uid={unitId}&cid={unitId}&tid={trackingId}&ua={userAgent}&qt={qt}&uip={IPAddress}");
    private String googleAnalyticsBaseUrl;
    private RestTemplate restTemplate;

    @Autowired
    public GoogleAnalyticsClient(
            @Value("${io.barracks.googleanalytics.base_url}") String googleAnalyticsBaseUrl,
            RestTemplateBuilder restTemplateBuilder
    ) {
        this.googleAnalyticsBaseUrl = googleAnalyticsBaseUrl;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendEventToGoogleAnalytics(String trackingId, String userAgent, String unitId, String key, String value, String uip) {

        final String v = "1";
        final String t = "event";
        final int qt = 0;

        try {
            restTemplate.exchange(
                    GOOGLE_ANALYTICS_ENDPOINT.withBase(googleAnalyticsBaseUrl).getRequestEntity(v, t, key, value, unitId, unitId, trackingId, userAgent, qt, uip),
                    String.class
            );

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new GoogleAnalyticsClientException(e);
        }
    }
}
