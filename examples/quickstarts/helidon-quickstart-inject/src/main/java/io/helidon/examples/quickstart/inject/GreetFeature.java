/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
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

package io.helidon.examples.quickstart.inject;

import io.helidon.service.registry.Service;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;

/**
 * Http features are automatically discovered by Helidon WebServer when running with
 * {@link io.helidon.service.registry.ServiceRegistryManager#start(io.helidon.service.registry.Binding}.
 */
@Service.Singleton
class GreetFeature implements HttpFeature {
    private final GreetService greetService;

    @Service.Inject
    GreetFeature(GreetService greetService) {
        this.greetService = greetService;
    }

    @Override
    public void setup(HttpRouting.Builder builder) {
        builder.register("/greet", greetService);
    }
}
