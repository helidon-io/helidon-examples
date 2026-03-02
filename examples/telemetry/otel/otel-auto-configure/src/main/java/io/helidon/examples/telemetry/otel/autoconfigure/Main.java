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

package io.helidon.examples.telemetry.otel.autoconfigure;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Helidon SE example application to illustrate direct use of the OpenTelemetry APIs.
 */
public class Main {
    private static final String INSTRUMENTATION_SCOPE = "ExampleOTelService";
    private static final String SERVICE_NAME_PROPERTY = "otel.service.name";

    private static final String DEFAULT_SERVICE_NAME = "helidon-example";

    private static Meter meter;
    private static Tracer tracer;
    private static TextMapPropagator propagator;

    private static LongCounter personalizedGreetingCounter;

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        // load logging configuration
        LogConfig.configureRuntime();

        // initialize config from default configuration
        Config config = Services.get(Config.class);

        // Sets up the OpenTelemetry meter, tracer, and propagator.
        prepareOpenTelemetryEnvironment(config);

        // Create a counter to count the number of requests for personalized greetings.
        personalizedGreetingCounter = meter.counterBuilder("example.greet.personalized")
                .setDescription("Count of personalized greetings sent")
                .build();

        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(Main::routing)
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port() + "/greet");

    }

    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing
                .addFilter(new ExampleFilter(propagator, tracer, meter))
                .get("/greet", (req, resp) -> resp.send("Hello World!"))
                .get("/greet/{name}", (req, resp) -> {
                    personalizedGreetingCounter.add(1L);
                    resp.send("Hello " + req.path().pathParameters().get("name"));
                });
    }


    static void prepareOpenTelemetryEnvironment(Config config) {
        var openTelemetry = prepareOpenTelemetry(config);

        tracer = openTelemetry.getTracer(INSTRUMENTATION_SCOPE);
        meter = openTelemetry.getMeter(INSTRUMENTATION_SCOPE);
        propagator = openTelemetry.getPropagators().getTextMapPropagator();
    }

    static OpenTelemetry prepareOpenTelemetry(Config config) {

        return AutoConfiguredOpenTelemetrySdk.builder()
                .addPropertiesSupplier(configBasedPropertySupplier(config))
                .setResultAsGlobal()
                .build()
                .getOpenTelemetrySdk();

    }

    static Supplier<Map<String, String>> configBasedPropertySupplier(Config config) {
        return () -> config.get("otel")
                .traverse()
                .filter(node -> node.type() == Config.Type.VALUE)
                .filter(node -> !node.key().toString().isBlank()
                        && node.asString().isPresent()
                        && !node.asString().get().isBlank())
                .collect(Collectors.toMap(
                        node -> node.key().toString(),
                        node -> node.asString().get()
                ));
    }

}
