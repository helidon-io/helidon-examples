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

import io.helidon.common.media.type.MediaTypes;
import io.helidon.common.testing.junit5.OptionalMatcher;
import io.helidon.http.Status;
import io.helidon.json.JsonObject;
import io.helidon.service.registry.ServiceRegistry;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
public class DeclarativeMetricsTest {
    private final Http1Client client;
    private final ServiceRegistry registry;

    public DeclarativeMetricsTest(Http1Client client, ServiceRegistry registry) {
        this.client = client;
        this.registry = registry;
    }

    @Test
    void testCounter() {
        var response = client.get("/hello/counted")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("counted"));

        var metricsResponse = client.get("/observe/metrics")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(JsonObject.class);

        assertThat(metricsResponse.status(), is(Status.OK_200));
        var json = metricsResponse.entity();

        String metricName = "MetricsEndpoint.counted;application=MetricsExample;endpoint=MetricsEndpoint;location=method";
        Optional<Integer> applicationOpt = json.objectValue("application")
                .flatMap(it -> it.intValue(metricName));
        assertThat(applicationOpt, OptionalMatcher.optionalValue(is(1)));
    }

    @Test
    void testTimed() {
        var response = client.get("/hello/timed")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("timed"));

        var metricsResponse = client.get("/observe/metrics")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(JsonObject.class);

        assertThat(metricsResponse.status(), is(Status.OK_200));
        var json = metricsResponse.entity();

        String metricName = "my-timed-metric";
        Optional<Integer> applicationOpt = json.objectValue("application")
                .flatMap(it -> it.objectValue(metricName))
                .flatMap(it -> it.intValue("count;application=MetricsExample;endpoint=MetricsEndpoint"));
        assertThat(applicationOpt, OptionalMatcher.optionalValue(is(1)));
    }

    @Test
    void testGauge() {
        // this is a run level service, and these are not automatically started in tests
        registry.get(MetricsEndpoint__GaugeRegistrar.class);

        var response = client.get("/hello/gauge")
                .accept(MediaTypes.TEXT_PLAIN)
                .queryParam("value", "42")
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.entity(), is("gauge set to 42"));

        var metricsResponse = client.get("/observe/metrics")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(JsonObject.class);

        assertThat(metricsResponse.status(), is(Status.OK_200));
        var json = metricsResponse.entity();

        String metricName = "MetricsEndpoint.gaugeValue;application=MetricsExample;endpoint=MetricsEndpoint";
        Optional<Integer> applicationOpt = json.objectValue("application")
                .flatMap(it -> it.intValue(metricName));
        assertThat(applicationOpt, OptionalMatcher.optionalValue(is(42)));
    }
}
