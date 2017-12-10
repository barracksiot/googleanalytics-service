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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;
import java.util.Optional;

@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class DeviceInfo {

    @NotBlank
    private final String unitId;

    private final String userId;

    private final String segmentId;

    @NotBlank
    private final String versionId;

    private final Date receptionDate;

    private final String deviceIP;

    private final String userAgent;

    private final ObjectNode additionalProperties;

    @JsonCreator
    public static DeviceInfo fromJson(
            @JsonProperty("receptionDate") Date receptionDate
    ) {
        return builder()
                .receptionDate(receptionDate == null ? null : new Date(receptionDate.getTime()))
                .build();
    }


    public ObjectNode getAdditionalProperties() {
        return Optional.ofNullable(additionalProperties).orElse(new ObjectNode(JsonNodeFactory.instance));
    }

    public Date getReceptionDate() {
        if (this.receptionDate != null) {
            return new Date(receptionDate.getTime());
        }
        return null;
    }

}
