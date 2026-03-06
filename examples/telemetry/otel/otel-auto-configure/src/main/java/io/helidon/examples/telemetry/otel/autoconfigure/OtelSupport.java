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

import java.util.Iterator;
import java.util.List;

import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.webserver.http.RoutingRequest;

import io.opentelemetry.context.propagation.TextMapGetter;

class OtelSupport {

    static final TextMapGetter<RoutingRequest> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(RoutingRequest carrier) {
            return carrier.headers().stream()
                    .map(Header::name)
                    .toList();
        }

        @Override
        public String get(RoutingRequest carrier, String key) {
            return carrier.headers().first(HeaderNames.create(key)).orElse(null);
        }

        @Override
        public Iterator<String> getAll(RoutingRequest carrier, String key) {
            return carrier.headers().all(HeaderNames.create(key), List::of).iterator();
        }
    };

    private OtelSupport() {
    }
}
