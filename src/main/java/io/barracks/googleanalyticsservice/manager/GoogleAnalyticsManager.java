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

package io.barracks.googleanalyticsservice.manager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.barracks.googleanalyticsservice.client.AuthorizationServiceClient;
import io.barracks.googleanalyticsservice.client.GoogleAnalyticsClient;
import io.barracks.googleanalyticsservice.client.exception.GoogleAnalyticsClientException;
import io.barracks.googleanalyticsservice.model.DeviceInfo;
import io.barracks.googleanalyticsservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GoogleAnalyticsManager {

    @Autowired
    private AuthorizationServiceClient authorizationServiceClient;
    @Autowired
    private GoogleAnalyticsClient googleAnalyticsClient;

    public void sendEventToGoogleAnalytics(DeviceInfo deviceInfo) {
        final User user = authorizationServiceClient.getUserById(deviceInfo.getUserId());
        if (!StringUtils.isEmpty(user.getGaTrackingId())) {
            final ObjectNode customClientData = deviceInfo.getAdditionalProperties();
            final List<String> prefix = new ArrayList<>();
            sendObjectKeyValues(user, deviceInfo, prefix, customClientData);
        }
    }

    private void sendObjectKeyValues(User user, DeviceInfo deviceInfo, List<String> prefix, ObjectNode node) {
        if(!node.fieldNames().hasNext()) {
            googleAnalyticsClient.sendEventToGoogleAnalytics(
                    user.getGaTrackingId(),
                    deviceInfo.getUserAgent(),
                    deviceInfo.getUnitId(),
                    deviceInfo.getVersionId(),
                    String.join(".", prefix),
                    "",
                    deviceInfo.getDeviceIP());
        }

        node.fields().forEachRemaining(entry -> {
            List<String> prefixCopy = new ArrayList<>(prefix);
            prefixCopy.add(entry.getKey());
            if (entry.getValue() instanceof ObjectNode) {
                sendObjectKeyValues(
                        user,
                        deviceInfo,
                        prefixCopy,
                        ((ObjectNode) entry.getValue())
                );
            } else if (entry.getValue() instanceof ValueNode) {
                try {
                    googleAnalyticsClient.sendEventToGoogleAnalytics(
                            user.getGaTrackingId(),
                            deviceInfo.getUserAgent(),
                            deviceInfo.getUnitId(),
                            deviceInfo.getVersionId(),
                            String.join(".", prefixCopy),
                            entry.getValue().asText(),
                            deviceInfo.getDeviceIP());
                } catch (GoogleAnalyticsClientException e) {
                    log.error("Problem sending custom client data | Value : " + entry.getValue().asText() + " | Key : " + entry.getKey(), e);
                }
            } else {
                log.error(" Value " + entry.getValue().asText() + " is not of the expected type. ");
            }
        });
    }
}
