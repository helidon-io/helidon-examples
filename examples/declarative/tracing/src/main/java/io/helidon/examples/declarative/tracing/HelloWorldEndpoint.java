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

package io.helidon.examples.declarative.tracing;

import java.util.concurrent.atomic.AtomicReference;

import io.helidon.common.Api;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.config.Configuration;
import io.helidon.http.Http;
import io.helidon.http.Status;
import io.helidon.service.registry.Service;
import io.helidon.tracing.Span;
import io.helidon.tracing.Tracing;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings(Api.SUPPRESS_INCUBATING)
@RestServer.Endpoint
@Http.Path("/hello")
@Service.Singleton
@Tracing.Traced(tags = @Tracing.Tag(key = "example", value = "declarative-tracing"), kind = Span.Kind.SERVER)
class HelloWorldEndpoint {
    private final AtomicReference<String> greeting = new AtomicReference<>();

    @Service.Inject
    HelloWorldEndpoint(@Configuration.Value("${app.greeting:Hello}") String greeting) {
        this.greeting.set(greeting);
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Tracing.Traced(value = "greet-world", tags = @Tracing.Tag(key = "route", value = "world"))
    String hello(@Http.HeaderParam("User-Agent") @Tracing.ParamTag String userAgent) {
        return greeting.get() + " World";
    }

    @Http.GET
    @Http.Path("/{name}")
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Tracing.Traced(value = "greet-name", tags = @Tracing.Tag(key = "route", value = "named"))
    String hello(@Http.PathParam("name") String name,
                 @Http.HeaderParam("User-Agent") @Tracing.ParamTag String userAgent) {
        return greeting.get() + " " + name;
    }

    @Http.POST
    @Http.Consumes(MediaTypes.TEXT_PLAIN_VALUE)
    @RestServer.Status(Status.NO_CONTENT_204_CODE)
    @Tracing.Traced(value = "update-greeting", tags = @Tracing.Tag(key = "route", value = "update"))
    void updateGreeting(@Http.Entity String newGreeting) {
        greeting.set(newGreeting);
    }
}
