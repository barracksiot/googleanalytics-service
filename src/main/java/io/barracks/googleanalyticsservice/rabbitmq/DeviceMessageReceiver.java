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

package io.barracks.googleanalyticsservice.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.googleanalyticsservice.manager.GoogleAnalyticsManager;
import io.barracks.googleanalyticsservice.model.DeviceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeviceMessageReceiver {
    private final GoogleAnalyticsManager googleAnalyticsManager;
    private final ObjectMapper objectMapper;

    @Autowired
    DeviceMessageReceiver(GoogleAnalyticsManager googleAnalyticsManager, ObjectMapper objectMapper) {
        this.googleAnalyticsManager = googleAnalyticsManager;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${io.barracks.googleanalytics.queuename}", durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = "${io.barracks.amqp.exchangename}", type = "topic", durable = "true"),
                    key = "${io.barracks.googleanalytics.routingkey}"
            )
    )
    public void receiveMessage(@Payload DeviceInfo deviceInfo) {
        try {
            googleAnalyticsManager.sendEventToGoogleAnalytics(deviceInfo);
        } catch (Exception e) {
            log.error("Error while sending data to google analytics", e);
        }
    }


}
