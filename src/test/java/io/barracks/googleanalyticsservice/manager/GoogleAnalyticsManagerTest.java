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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.googleanalyticsservice.client.GoogleAnalyticsClient;
import io.barracks.googleanalyticsservice.client.exception.GoogleAnalyticsClientException;
import io.barracks.googleanalyticsservice.model.*;
import io.barracks.googleanalyticsservice.utils.DeviceRequestUtils;
import io.barracks.googleanalyticsservice.utils.HookUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAnalyticsManagerTest {

    @Mock
    private GoogleAnalyticsClient googleAnalyticsClient;

    @InjectMocks
    private GoogleAnalyticsManager googleAnalyticsManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void sendDataToGoogleAnalytics_shouldCallClients_andReturnsNothing() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceEventHook.json", getClass());
        final DeviceEventHook deviceEventHook = objectMapper.readValue(resource.getInputStream(), DeviceEventHook.class);
        final DeviceRequest deviceRequest = deviceEventHook.getDeviceEvent().getRequest();
        final Hook hook = deviceEventHook.getHook();

        Map<String, String> keyValues = new HashMap<String, String>();
        keyValues.put("battery.level", "50");
        keyValues.put("battery.damaged", "false");
        keyValues.put("battery.brand", "Apple");
        keyValues.put("battery.useCases.normal", "yes");
        keyValues.put("battery.useCases.time", "12.4");
        keyValues.put("battery.useCases.beaver", "true");
        keyValues.put("battery.useCases.yes.yes", "true");
        keyValues.put("battery.useCases.yes.no", "false");
        keyValues.put("battery.useCases.oui", "oui");
        keyValues.put("elephants.animal", "big");
        keyValues.put("what", "this");
        keyValues.put("howMuch", "3.5");
        keyValues.put("Reference of the first package", "Version for the first package");
        keyValues.put("Reference of the second package", "Version for the second package");

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceEventHook);

        // When / Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(googleAnalyticsClient, new Times(14)).sendEventToGoogleAnalytics(
                eq(hook.getGaTrackingId()),
                eq(deviceRequest.getUserAgent()),
                eq(deviceRequest.getUnitId()),
                keyCaptor.capture(),
                valueCaptor.capture(),
                eq(deviceRequest.getIpAddress()));

        assertThat(keyCaptor.getAllValues()).containsOnlyElementsOf(keyValues.keySet());
        assertThat(valueCaptor.getAllValues()).containsOnlyElementsOf(keyValues.values());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenEmptyCustomClientData_shouldSendToGoogleAnalyticsEmptyEvent() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceEventHookEmptyCustomClientData.json", getClass());
        final DeviceEventHook deviceEventHook = objectMapper.readValue(resource.getInputStream(), DeviceEventHook.class);
        final DeviceRequest deviceRequest = deviceEventHook.getDeviceEvent().getRequest();
        final Hook hook = deviceEventHook.getHook();

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceEventHook);

        // When / Then
        verify(googleAnalyticsClient, new Times(1)).sendEventToGoogleAnalytics(
                hook.getGaTrackingId(),
                deviceRequest.getUserAgent(),
                deviceRequest.getUnitId(),
                "",
                "",
                deviceRequest.getIpAddress());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenNoCustomClientData_shouldSendToGoogleAnalyticsEmptyEvent() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceEventHookNoCustomClientData.json", getClass());
        final DeviceEventHook deviceEventHook = objectMapper.readValue(resource.getInputStream(), DeviceEventHook.class);
        final DeviceRequest deviceRequest = deviceEventHook.getDeviceEvent().getRequest();
        final Hook hook = deviceEventHook.getHook();

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceEventHook);

        // When / Then
        verify(googleAnalyticsClient, new Times(1)).sendEventToGoogleAnalytics(
                hook.getGaTrackingId(),
                deviceRequest.getUserAgent(),
                deviceRequest.getUnitId(),
                "",
                "",
                deviceRequest.getIpAddress());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenValueMissingForAKey_shouldSendItAnywayWithNoValue() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceEventHookMissingValueForAKey.json", getClass());
        final DeviceEventHook deviceEventHook = objectMapper.readValue(resource.getInputStream(), DeviceEventHook.class);
        final DeviceRequest deviceRequest = deviceEventHook.getDeviceEvent().getRequest();
        final Hook hook = deviceEventHook.getHook();

        Map<String, String> keyValues = new HashMap<String, String>();
        keyValues.put("battery.level", "50");
        keyValues.put("battery.damaged", "false");
        keyValues.put("battery.brand", "Apple");
        keyValues.put("battery.useCases", "");
        keyValues.put("elephants.animal", "big");
        keyValues.put("what", "this");
        keyValues.put("howMuch", "3.5");
        keyValues.put("Reference of the first package", "Version for the first package");
        keyValues.put("Reference of the second package", "Version for the second package");

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceEventHook);

        // When / Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(googleAnalyticsClient, new Times(9)).sendEventToGoogleAnalytics(
                eq(hook.getGaTrackingId()),
                eq(deviceRequest.getUserAgent()),
                eq(deviceRequest.getUnitId()),
                keyCaptor.capture(),
                valueCaptor.capture(),
                eq(deviceRequest.getIpAddress()));

        assertThat(keyCaptor.getAllValues()).containsOnlyElementsOf(keyValues.keySet());
        assertThat(valueCaptor.getAllValues()).containsOnlyElementsOf(keyValues.values());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenExceptionIsThrown_shouldLogError() throws IOException {
        final ClassPathResource resource = new ClassPathResource("deviceEventHookMissingValueForAKey.json", getClass());
        final DeviceEventHook deviceEventHook = objectMapper.readValue(resource.getInputStream(), DeviceEventHook.class);
        final DeviceRequest deviceRequest = deviceEventHook.getDeviceEvent().getRequest();
        final Hook hook = HookUtils.getHook();

        doThrow(GoogleAnalyticsClientException.class).when(googleAnalyticsClient).sendEventToGoogleAnalytics(
                anyString(),
                anyString(),
                anyString(),
                eq(deviceRequest.getPackages().get(0).getReference()),
                eq(deviceRequest.getPackages().get(0).getVersion().get()),
                anyString());
        doThrow(GoogleAnalyticsClientException.class).when(googleAnalyticsClient).sendEventToGoogleAnalytics(
                anyString(),
                anyString(),
                anyString(),
                eq("what"),
                eq("this"),
                anyString());
        Map<String, String> keyValues = new HashMap<>();
        keyValues.put("battery.level", "50");
        keyValues.put("battery.damaged", "false");
        keyValues.put("battery.brand", "Apple");
        keyValues.put("battery.useCases", "");
        keyValues.put("elephants.animal", "big");
        keyValues.put("what", "this");
        keyValues.put("howMuch", "3.5");
        keyValues.put("Reference of the second package", "Version for the second package");
        keyValues.put("Reference of the first package", "Version for the first package");

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceEventHook);

        // When / Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(googleAnalyticsClient, new Times(9)).sendEventToGoogleAnalytics(
                eq(hook.getGaTrackingId()),
                eq(deviceRequest.getUserAgent()),
                eq(deviceRequest.getUnitId()),
                keyCaptor.capture(),
                valueCaptor.capture(),
                eq(deviceRequest.getIpAddress()));


        assertThat(keyCaptor.getAllValues()).containsOnlyElementsOf(keyValues.keySet());
        assertThat(valueCaptor.getAllValues()).containsOnlyElementsOf(keyValues.values());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenDeviceChangeEvent_shouldCallClients_andReturnsNothing() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceChangeEventHook.json", getClass());
        final DeviceChangeEventHook deviceChangeEventHook = objectMapper.readValue(resource.getInputStream(), DeviceChangeEventHook.class);
        final DeviceRequest deviceRequest = deviceChangeEventHook.getDeviceChangeEvent().getDeviceEvent().getRequest();
        final Hook hook = deviceChangeEventHook.getHook();

        Map<String, String> newKeyValues = new HashMap<String, String>();
        newKeyValues.put("oldData.level", "45");
        newKeyValues.put("oldData.damaged", "true");
        newKeyValues.put("oldData.useCases.normal", "Not normal");
        newKeyValues.put("oldData.useCases.time", "54.6");
        newKeyValues.put("oldData.useCases.yes.no", "no");
        newKeyValues.put("howMany", "A lot");
        newKeyValues.put("Reference of the old package", "Version for the old package");

        newKeyValues.put("battery.level", "50");
        newKeyValues.put("battery.damaged", "false");
        newKeyValues.put("battery.brand", "Apple");
        newKeyValues.put("battery.useCases.normal", "yes");
        newKeyValues.put("battery.useCases.time", "12.4");
        newKeyValues.put("battery.useCases.beaver", "true");
        newKeyValues.put("battery.useCases.yes.yes", "true");
        newKeyValues.put("battery.useCases.yes.no", "false");
        newKeyValues.put("battery.useCases.oui", "oui");
        newKeyValues.put("elephants.animal", "big");
        newKeyValues.put("what", "this");
        newKeyValues.put("howMuch", "3.5");
        newKeyValues.put("Reference of the first package", "Version for the first package");
        newKeyValues.put("Reference of the second package", "Version for the second package");

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceChangeEventHook);

        // When / Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(googleAnalyticsClient, new Times(21)).sendEventToGoogleAnalytics(
                eq(hook.getGaTrackingId()),
                eq(deviceRequest.getUserAgent()),
                eq(deviceRequest.getUnitId()),
                keyCaptor.capture(),
                valueCaptor.capture(),
                eq(deviceRequest.getIpAddress()));

        assertThat(keyCaptor.getAllValues()).containsOnlyElementsOf(newKeyValues.keySet());
        assertThat(valueCaptor.getAllValues()).containsOnlyElementsOf(newKeyValues.values());

    }


}
