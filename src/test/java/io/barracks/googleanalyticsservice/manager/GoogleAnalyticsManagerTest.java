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
import io.barracks.googleanalyticsservice.client.AuthorizationServiceClient;
import io.barracks.googleanalyticsservice.client.GoogleAnalyticsClient;
import io.barracks.googleanalyticsservice.model.DeviceInfo;
import io.barracks.googleanalyticsservice.model.User;
import io.barracks.googleanalyticsservice.utils.DeviceInfoUtils;
import io.barracks.googleanalyticsservice.utils.UserUtils;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GoogleAnalyticsManagerTest {

    @Mock
    private AuthorizationServiceClient authorizationServiceClient;
    @Mock
    private GoogleAnalyticsClient googleAnalyticsClient;

    @InjectMocks
    private GoogleAnalyticsManager googleAnalyticsManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void sendDataToGoogleAnalytics_shouldCallClients_andReturnsNothing() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceInfo.json", getClass());
        final DeviceInfo deviceInfo = objectMapper.readValue(resource.getInputStream(), DeviceInfo.class);

        final User user = UserUtils.getUser();
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

        final String userId = deviceInfo.getUserId();
        doReturn(user).when(authorizationServiceClient).getUserById(userId);

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceInfo);

        // When / Then
        verify(authorizationServiceClient).getUserById(userId);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(googleAnalyticsClient, new Times(12)).sendEventToGoogleAnalytics(
                eq(user.getGaTrackingId()),
                eq(deviceInfo.getUserAgent()),
                eq(deviceInfo.getUnitId()),
                eq(deviceInfo.getVersionId()),
                keyCaptor.capture(),
                valueCaptor.capture(),
                eq(deviceInfo.getDeviceIP()));

        assertThat(keyCaptor.getAllValues()).containsOnlyElementsOf(keyValues.keySet());
        assertThat(valueCaptor.getAllValues()).containsOnlyElementsOf(keyValues.values());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenUserHasNoTrackingId_ShouldDoNothing() throws IOException {
        // Given
        final DeviceInfo deviceInfo = DeviceInfoUtils.getDeviceInfo();
        final User user = User.builder().gaTrackingId(null).build();
        doReturn(user).when(authorizationServiceClient).getUserById(any());

        //When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceInfo);

        //Then
        verify(googleAnalyticsClient, never()).sendEventToGoogleAnalytics(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void sendDataToGoogleAnalytics_whenEmptyCustomClientData_ShouldSendToGoogleAnalyticsEmptyEvent() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceInfoEmptyCustomClientData.json", getClass());
        final DeviceInfo deviceInfo = objectMapper.readValue(resource.getInputStream(), DeviceInfo.class);

        final User user = UserUtils.getUser();

        final String userId = deviceInfo.getUserId();
        doReturn(user).when(authorizationServiceClient).getUserById(userId);

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceInfo);

        // When / Then
        verify(authorizationServiceClient).getUserById(userId);
        verify(googleAnalyticsClient, new Times(1)).sendEventToGoogleAnalytics(
                user.getGaTrackingId(),
                deviceInfo.getUserAgent(),
                deviceInfo.getUnitId(),
                deviceInfo.getVersionId(),
                "",
                "",
                deviceInfo.getDeviceIP());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenNoCustomClientData_ShouldSendToGoogleAnalyticsEmptyEvent() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceInfoNoCustomClientData.json", getClass());
        final DeviceInfo deviceInfo = objectMapper.readValue(resource.getInputStream(), DeviceInfo.class);

        final User user = UserUtils.getUser();

        final String userId = deviceInfo.getUserId();
        doReturn(user).when(authorizationServiceClient).getUserById(userId);

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceInfo);

        // When / Then
        verify(authorizationServiceClient).getUserById(userId);
        verify(googleAnalyticsClient, new Times(1)).sendEventToGoogleAnalytics(
                user.getGaTrackingId(),
                deviceInfo.getUserAgent(),
                deviceInfo.getUnitId(),
                deviceInfo.getVersionId(),
                "",
                "",
                deviceInfo.getDeviceIP());

    }

    @Test
    public void sendDataToGoogleAnalytics_whenValueMissingForAKey_ShouldSendItAnywayWithNoValue() throws IOException {
        // Given
        final ClassPathResource resource = new ClassPathResource("deviceInfoMissingValueForAKey.json", getClass());
        final DeviceInfo deviceInfo = objectMapper.readValue(resource.getInputStream(), DeviceInfo.class);

        final User user = UserUtils.getUser();
        Map<String, String> keyValues = new HashMap<String, String>();
        keyValues.put("battery.level", "50");
        keyValues.put("battery.damaged", "false");
        keyValues.put("battery.brand", "Apple");
        keyValues.put("battery.useCases", "");
        keyValues.put("elephants.animal", "big");
        keyValues.put("what", "this");
        keyValues.put("howMuch", "3.5");

        final String userId = deviceInfo.getUserId();
        doReturn(user).when(authorizationServiceClient).getUserById(userId);

        // When
        googleAnalyticsManager.sendEventToGoogleAnalytics(deviceInfo);

        // When / Then
        verify(authorizationServiceClient).getUserById(userId);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(googleAnalyticsClient, new Times(7)).sendEventToGoogleAnalytics(
                eq(user.getGaTrackingId()),
                eq(deviceInfo.getUserAgent()),
                eq(deviceInfo.getUnitId()),
                eq(deviceInfo.getVersionId()),
                keyCaptor.capture(),
                valueCaptor.capture(),
                eq(deviceInfo.getDeviceIP()));

        assertThat(keyCaptor.getAllValues()).containsOnlyElementsOf(keyValues.keySet());
        assertThat(valueCaptor.getAllValues()).containsOnlyElementsOf(keyValues.values());

    }



}
