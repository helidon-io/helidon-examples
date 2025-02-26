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

import java.util.ArrayList;
import java.util.List;

import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryConfig;
import io.helidon.service.registry.ServiceRegistryManager;

/**
 * An example that illustrates {@link Service.RunLevel}.
 */
class RunLevelExample {

    static final List<String> STARTUP_EVENTS = new ArrayList<>();

    private RunLevelExample() {
    }

    public static void main(String[] args) {
        var injectConfig = ServiceRegistryConfig.builder()
                .maxRunLevel(2)
                .build();
        var manager = ServiceRegistryManager.start(injectConfig);
        STARTUP_EVENTS.forEach(System.out::println);
        manager.shutdown();
    }

    /**
     * A service that starts at level {@code 1}.
     */
    @Service.RunLevel(1)
    @Service.Singleton
    static class Level1 {

        @Service.PostConstruct
        void onCreate() {
            STARTUP_EVENTS.add("level1");
        }
    }

    /**
     * A service that starts at level {@code 2}.
     */
    @Service.RunLevel(2)
    @Service.Singleton
    static class Level2 {

        @Service.PostConstruct
        void onCreate() {
            STARTUP_EVENTS.add("level2");
        }
    }
}
