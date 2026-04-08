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

package io.helidon.examples.declarative.validation;

import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.common.Api;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.validation.Validation;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings(Api.SUPPRESS_INCUBATING) // Helidon declarative is an incubating feature
@RestServer.Endpoint // webserver declarative endpoint
@Http.Path("/validate") // path to serve this endpoint on
@Service.Singleton // service registry scope (must be singleton)
class ValidationEndpoint {
    private final AtomicInteger gaugeValue = new AtomicInteger();

    ValidationEndpoint() {
    }

    @Http.POST
    @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    MyDto validate(@Validation.Valid @Http.Entity MyDto dto) {
        return dto;
    }

}
