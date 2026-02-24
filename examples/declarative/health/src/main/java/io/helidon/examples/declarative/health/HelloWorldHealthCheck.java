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

package io.helidon.examples.declarative.health;

import io.helidon.health.HealthCheck;
import io.helidon.health.HealthCheckResponse;
import io.helidon.health.HealthCheckType;
import io.helidon.service.registry.Service;

/**
 * This health check returns UP when the greeting is set to {@code Hello},
 * and DOWN otherwise. If down, the returned data contains the greeting.
 * <p>
 * Note that entity is returned only if the {code health.details} is set to true in {@code application.yaml}.
 */
@Service.Singleton
class HelloWorldHealthCheck implements HealthCheck {
    private final HelloWorldEndpoint endpoint;

    @Service.Inject
    HelloWorldHealthCheck(HelloWorldEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public HealthCheckResponse call() {
        var current = endpoint.greeting();
        if ("Hello".equals(current)) {
            return HealthCheckResponse.builder()
                    .status(HealthCheckResponse.Status.UP)
                    .build();
        }
        return HealthCheckResponse.builder()
                .status(HealthCheckResponse.Status.DOWN)
                .detail("greeting", current)
                .build();
    }

    @Override
    public HealthCheckType type() {
        return HealthCheckType.LIVENESS;
    }

    @Override
    public String name() {
        return "greeting";
    }

    @Override
    public String path() {
        return "greeting";
    }
}
