/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.declarative.metrics;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Http;
import io.helidon.metrics.api.Meter;
import io.helidon.metrics.api.Metrics;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings("deprecation")
@RestServer.Endpoint // webserver declarative endpoint
@Http.Path("/hello") // path to serve this endpoint on
@Service.Singleton // service registry scope (must be singleton)
@Metrics.Tag(key = "endpoint", value = "MetricsEndpoint")
@Metrics.Tag(key = "application", value = "MetricsExample")
class MetricsEndpoint {
    private final AtomicInteger gaugeValue = new AtomicInteger();

    MetricsEndpoint() {
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/counted")
    @Metrics.Counted(tags = @Metrics.Tag(key = "location", value = "method"))
    String counted() {
        return "counted";
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/timed")
    @Metrics.Timed(value = "my-timed-metric", absoluteName = true)
    String timed() {
        return "timed";
    }

    @Http.GET
    @Http.Path("/gauge")
    String gauge(@Http.QueryParam("value") Optional<Integer> value) {
        gaugeValue.set(value.orElse(0));
        return "gauge set to " + value.orElse(0);
    }

    @Metrics.Gauge(unit = Meter.BaseUnits.BYTES)
    int gaugeValue() {
        return gaugeValue.get();
    }
}
