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

import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * Example filter to add a span and update a histogram (timer) for each incoming HTTP request.
 */
class ExampleFilter implements Filter {

    private final TextMapPropagator propagator;
    private final Tracer tracer;
    private final DoubleHistogram histogram;


    ExampleFilter(TextMapPropagator propagator, Tracer tracer, Meter meter) {
        this.propagator = propagator;
        this.tracer = tracer;
        histogram = meter.histogramBuilder("example.HTTP.elapsed")
                .setDescription("HTTP elapsed time in ms")
                .setUnit("ms")
                .build();
    }

    @Override
    public void filter(FilterChain filterChain, RoutingRequest routingRequest, RoutingResponse routingResponse) {

        var startTime = System.nanoTime();

        /*
        Either update to a new context based on the propagated context from the incoming headers, if the context headers are
        present, or retain the same context otherwise.
         */
        var inboundSpanContext = propagator.extract(Context.current(),
                                                    routingRequest,
                                                    OtelSupport.GETTER);

        try (var ignored = inboundSpanContext.makeCurrent()) {
            var span = tracer.spanBuilder("HTTP-request")
                    .setSpanKind(SpanKind.SERVER)
                    .setAttribute("path", routingRequest.path().path())
                    .startSpan();

            try {
                filterChain.proceed();
                span.setStatus(StatusCode.OK);
            } catch (Throwable throwable) {
                span.setStatus(StatusCode.ERROR, throwable.getMessage());
            } finally {
                var endTIme = System.nanoTime();
                var route = routingRequest.matchingPattern().orElseGet(() -> routingRequest.path().path());
                Attributes attrs = Attributes.of(
                        AttributeKey.stringKey("http.method"), routingRequest.prologue().method().text(),
                        AttributeKey.stringKey("http.route"), route);
                histogram.record(endTIme - startTime, attrs, Context.current());
                span.setAttribute(AttributeKey.stringKey("http.path"), routingRequest.path().path());
                span.setAttribute(AttributeKey.stringKey("http.route"), route);
                span.end();
            }
        }
    }
}
