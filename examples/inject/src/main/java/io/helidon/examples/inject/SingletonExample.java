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

import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates usages of {@link Service.Singleton}.
 */
class SingletonExample {

    private SingletonExample() {
    }

    public static void main(String[] args) {
        var myService1 = Services.get(MySingleton.class);
        var myService2 = Services.get(MySingleton.class);

        System.out.printf("%s - %s%n", System.identityHashCode(myService1), System.identityHashCode(myService2));
    }

    /**
     * A singleton service.
     */
    @Service.Singleton
    static class MySingleton {
    }
}
