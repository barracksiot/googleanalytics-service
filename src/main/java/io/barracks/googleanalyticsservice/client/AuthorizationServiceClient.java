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
import io.barracks.googleanalyticsservice.client.exception.AuthorizationServiceClientException;
import io.barracks.googleanalyticsservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthorizationServiceClient {

    static final Endpoint GET_USER_ID_ENDPOINT = Endpoint.from(HttpMethod.GET, "/users/{uuid}");

    private String authorizationServiceBaseUrl;

    private RestTemplate restTemplate;

    @Autowired
    public AuthorizationServiceClient(
            @Value("${io.barracks.authorizationservice.base_url}") String authorizationServiceBaseUrl,
            RestTemplateBuilder restTemplateBuilder
    ) {
        this.authorizationServiceBaseUrl = authorizationServiceBaseUrl;
        this.restTemplate = restTemplateBuilder.build();
    }

    public User getUserById(String userId) {
        try {
            final ResponseEntity<User> responseEntity = restTemplate.exchange(
                    GET_USER_ID_ENDPOINT.withBase(authorizationServiceBaseUrl).getRequestEntity(userId),
                    User.class
            );
            return responseEntity.getBody();
        } catch (HttpStatusCodeException e) {
            throw new AuthorizationServiceClientException(e);
        }
    }

}
