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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.commons.util.Endpoint;
import io.barracks.googleanalyticsservice.client.exception.GoogleAnalyticsClientException;
import io.barracks.googleanalyticsservice.model.DeviceInfo;
import io.barracks.googleanalyticsservice.model.User;
import io.barracks.googleanalyticsservice.utils.DeviceInfoUtils;
import io.barracks.googleanalyticsservice.utils.UserUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.UUID;

import static io.barracks.googleanalyticsservice.client.GoogleAnalyticsClient.GOOGLE_ANALYTICS_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@RestClientTest(GoogleAnalyticsClient.class)
public class GoogleAnalyticsClientTest {

    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private GoogleAnalyticsClient googleAnalyticsClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${io.barracks.googleanalytics.base_url}")
    private String baseUrl;

    @Test
    public void sendDataToGoogleAnalytics_whenServiceFails_shouldThrowException() {
        // Given
        final User user = UserUtils.getUser();
        final DeviceInfo deviceInfo = DeviceInfoUtils.getDeviceInfo();
        final String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        final String v = "1";
        final String t = "event";
        final String ec = key;
        final String ea = value;
        final String uid = deviceInfo.getUnitId();
        final String tid = user.getGaTrackingId();
        final String ua = deviceInfo.getUserAgent();
        final String pr1cd1 = deviceInfo.getVersionId();
        final String uip = deviceInfo.getDeviceIP();
        final int qt = 0;

        mockServer.expect(method(GOOGLE_ANALYTICS_ENDPOINT.getMethod()))
                .andExpect(requestTo(GOOGLE_ANALYTICS_ENDPOINT.withBase(baseUrl).getURI(v, t, ec, ea, uid, uid, tid, ua, pr1cd1, qt, uip)))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        // When
        assertThatExceptionOfType(GoogleAnalyticsClientException.class)
                .isThrownBy(() -> googleAnalyticsClient.sendEventToGoogleAnalytics(
                        user.getGaTrackingId(),
                        deviceInfo.getUserAgent(),
                        deviceInfo.getUnitId(),
                        deviceInfo.getVersionId(),
                        key,
                        value,
                        uip)
                );

        // Then
        mockServer.verify();
    }

    @Test
    public void sendDataToGoogleAnalytics_whenServiceSucceeds_shouldReturnUserProfile() throws Exception {
        // Given
        final User user = UserUtils.getUser();
        final DeviceInfo deviceInfo = DeviceInfoUtils.getDeviceInfo();
        final String key = UUID.randomUUID().toString();
        final String value = UUID.randomUUID().toString();

        final String v = "1";
        final String t = "event";
        final String ec = key;
        final String ea = value;
        final String uid = deviceInfo.getUnitId();
        final String tid = user.getGaTrackingId();
        final String ua = deviceInfo.getUserAgent();
        final String pr1cd1 = deviceInfo.getVersionId();
        final String uip = deviceInfo.getDeviceIP();
        final int qt = 0;

        final Endpoint endpoint = GOOGLE_ANALYTICS_ENDPOINT;
        mockServer.expect(method(endpoint.getMethod()))
                .andExpect(requestTo(endpoint.withBase(baseUrl).getURI(v, t, ec, ea, uid, uid, tid, ua, pr1cd1, qt, uip)))
                .andRespond(withStatus(HttpStatus.OK));

        // When
        googleAnalyticsClient.sendEventToGoogleAnalytics(
                user.getGaTrackingId(),
                deviceInfo.getUserAgent(),
                deviceInfo.getUnitId(),
                deviceInfo.getVersionId(),
                key,
                value,
                uip);
        // Then
        mockServer.verify();
    }

}
