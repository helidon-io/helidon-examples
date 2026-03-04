/*
 * Copyright (c) 2025, 2026 Oracle and/or its affiliates.
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

package io.helidon.examples.declarative.webclient;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Http;

/**
 * API used by both server and client.
 * This API should only use annotations from {@code io.helidon.http.Http} class.
 */
@SuppressWarnings("deprecation")
@Http.Path("/hello") // path this endpoint is served on
interface HelloWorldApi {
    @Http.GET
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    GreetingDto hello();

    @Http.GET
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Path("/{name}")
    GreetingDto hello(@Http.PathParam("name") String name);

    @Http.Consumes(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.POST
    void updateGreeting(@Http.Entity String newGreeting);
}
