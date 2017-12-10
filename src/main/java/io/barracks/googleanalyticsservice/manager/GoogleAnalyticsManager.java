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
import io.barracks.googleanalyticsservice.client.GoogleAnalyticsClient;
import io.barracks.googleanalyticsservice.client.exception.GoogleAnalyticsClientException;
import io.barracks.googleanalyticsservice.model.*;
import io.barracks.googleanalyticsservice.model.Package;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GoogleAnalyticsManager {

    @Autowired
    private GoogleAnalyticsClient googleAnalyticsClient;

    public void sendEventToGoogleAnalytics(DeviceEventHook deviceEventHook) {
        final DeviceRequest deviceRequest = deviceEventHook.getDeviceEvent().getRequest();
        final ObjectNode customClientData = deviceRequest.getCustomClientData();
        final List<String> prefix = new ArrayList<>();
        sendObjectKeyValues(deviceRequest, deviceEventHook.getHook(), prefix, customClientData);

        final List<Package> packageList = deviceRequest.getPackages();
        packageList.forEach(
                packageRef ->
                        sendReferenceAndVersion(
                                deviceRequest,
                                deviceEventHook.getHook(),
                                packageRef.getReference(),
                                packageRef.getVersion().get()
                        )
        );
    }

    public void sendEventToGoogleAnalytics(DeviceChangeEventHook deviceChangeEventHook) {
        final DeviceRequest oldRequest = deviceChangeEventHook.getDeviceChangeEvent().getOldRequest();
        final DeviceRequest deviceRequest = deviceChangeEventHook.getDeviceChangeEvent().getDeviceEvent().getRequest();
        final ObjectNode oldData = oldRequest.getCustomClientData();
        final ObjectNode customClientData = deviceRequest.getCustomClientData();
        final List<String> prefix = new ArrayList<>();
        sendObjectKeyValues(oldRequest, deviceChangeEventHook.getHook(), prefix, oldData);
        sendObjectKeyValues(deviceRequest, deviceChangeEventHook.getHook(), prefix, customClientData);

        oldRequest.getPackages().forEach(
                packageRef ->
                        sendReferenceAndVersion(
                                deviceRequest,
                                deviceChangeEventHook.getHook(),
                                packageRef.getReference(),
                                packageRef.getVersion().get()
                        )
        );

        deviceRequest.getPackages().forEach(
                packageRef ->
                        sendReferenceAndVersion(
                                deviceRequest,
                                deviceChangeEventHook.getHook(),
                                packageRef.getReference(),
                                packageRef.getVersion().get()
                        )
        );
    }

    private void sendObjectKeyValues(DeviceRequest deviceRequest, Hook hook, List<String> prefix, ObjectNode node) {
        if (!node.fieldNames().hasNext()) {
            googleAnalyticsClient.sendEventToGoogleAnalytics(
                    hook.getGaTrackingId(),
                    deviceRequest.getUserAgent(),
                    deviceRequest.getUnitId(),
                    String.join(".", prefix),
                    "",
                    deviceRequest.getIpAddress());
        }

        node.fields().forEachRemaining(entry -> {
            List<String> prefixCopy = new ArrayList<>(prefix);
            prefixCopy.add(entry.getKey());
            if (entry.getValue() instanceof ObjectNode) {
                sendObjectKeyValues(
                        deviceRequest,
                        hook,
                        prefixCopy,
                        ((ObjectNode) entry.getValue())
                );
            } else if (entry.getValue() instanceof ValueNode) {
                try {
                    googleAnalyticsClient.sendEventToGoogleAnalytics(
                            hook.getGaTrackingId(),
                            deviceRequest.getUserAgent(),
                            deviceRequest.getUnitId(),
                            String.join(".", prefixCopy),
                            entry.getValue().asText(),
                            deviceRequest.getIpAddress());
                } catch (GoogleAnalyticsClientException e) {
                    log.error("Problem sending data | Value : " + entry.getValue().asText() + " | Key : " + entry.getKey(), e);
                }
            } else {
                log.error(" Value " + entry.getValue().asText() + " is not of the expected type. ");
            }
        });
    }

    private void sendReferenceAndVersion(DeviceRequest deviceRequest, Hook hook, String reference, String version) {
        try {
            googleAnalyticsClient.sendEventToGoogleAnalytics(
                    hook.getGaTrackingId(),
                    deviceRequest.getUserAgent(),
                    deviceRequest.getUnitId(),
                    reference,
                    version,
                    deviceRequest.getIpAddress());
        } catch (GoogleAnalyticsClientException e) {
            log.error("Problem sending data | Value : " + version + " | Key : " + reference, e);
        }

    }
}
