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

import java.util.function.Supplier;

import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates usages of {@link Service.PerLookup} without scope.
 */
class PerLookupExample {

    private PerLookupExample() {
    }

    public static void main(String[] args) {
        var myInstance1 = Services.get(MyInstance.class);
        var myInstance2 = Services.get(MyInstance.class);

        System.out.printf("%s - %s%n",
                System.identityHashCode(myInstance1),
                System.identityHashCode(myInstance2));

        var mySingleton = Services.get(MySingleton.class);

        System.out.printf("%s - %s%n",
                System.identityHashCode(mySingleton.instance().get()),
                System.identityHashCode(mySingleton.instance().get()));
    }

    /**
     * A service with the per-lookup scope.
     */
    @Service.PerLookup
    static class MyInstance {
    }

    /**
     * A singleton service.
     *
     * @param instance supplier of the service
     */
    @Service.Singleton
    record MySingleton(Supplier<MyInstance> instance) {
    }
}
