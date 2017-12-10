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

package io.barracks.googleanalyticsservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.barracks.googleanalyticsservice.utils.DeviceInfoUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class DeviceRequestDetailsJsonTests {

    @Autowired
    private JacksonTester<DeviceInfo> json;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("classpath:io/barracks/googleanalyticsservice/model/deviceInfo.json")
    private Resource deviceRequestDetails;

    @Test
    public void deserializeJson_shouldDeserialize() throws Exception {
        // Given
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();

        final DeviceInfo expected = DeviceInfoUtils.getDeviceInfo().toBuilder()
                .userId("ID of the user")
                .segmentId("ID of the segment")
                .receptionDate(null)
                .additionalProperties(root)
                .unitId("ID transmitted by the device")
                .versionId("Version of the device")
                .deviceIP("IP address of the device")
                .userAgent("Version of the SDK installed on the device that sent the information")
                .build();

        // When
        final DeviceInfo result = objectMapper.readValue(deviceRequestDetails.getInputStream(), DeviceInfo.class);


        // Then
        assertThat(expected).hasNoNullFieldsOrPropertiesExcept("receptionDate");
        assertThat(result).isEqualTo(expected);
    }

}
