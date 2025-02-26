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
package io.helidon.examples.inject;

import java.util.Map;
import java.util.function.Supplier;

import io.helidon.service.registry.Scope;
import io.helidon.service.registry.Scopes;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates usages of {@link Service.PerRequest}.
 */
class PerRequestExample {

    private PerRequestExample() {
    }

    public static void main(String[] args) {
        var myService = Services.get(MyService.class);
        var scopes = Services.get(Scopes.class);

        try (Scope ignored = scopes.createScope(Service.PerRequest.TYPE, "test-1", Map.of())) {
            System.out.println(myService.contract().get().sayHello());
        }
    }

    /**
     * A service in request scope.
     */
    @Service.PerRequest
    static class MyRequestScopeService {

        String sayHello() {
            return "Hello World!";
        }
    }

    /**
     * A singleton service that consumes a service in request scope.
     *
     * @param contract request scope supplier
     */
    @Service.Singleton
    record MyService(Supplier<MyRequestScopeService> contract) {
    }
}
