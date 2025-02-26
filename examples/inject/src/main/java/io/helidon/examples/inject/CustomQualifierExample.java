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
 * An example that illustrates usages of {@link Service.Qualifier}.
 */
class CustomQualifierExample {

    private CustomQualifierExample() {
    }

    public static void main(String[] args) {
        var blueDrawing = Services.get(BlueDrawing.class);
        var greenDrawing = Services.get(GreenDrawing.class);

        System.out.printf("blue drawing: %s%n", blueDrawing.circle().name());
        System.out.printf("green drawing: %s%n", greenDrawing.circle().name());
    }

    /**
     * A service to be implemented by qualified services.
     */
    interface Circle {
        String name();
    }

    /**
     * A custom qualifier annotation.
     */
    @Service.Qualifier
    public @interface Blue {
    }

    /**
     * A custom qualifier annotation.
     */
    @Service.Qualifier
    public @interface Green {
    }

    /**
     * A qualified service.
     */
    @Blue
    @Service.Singleton
    static class BlueCircle implements Circle {

        @Override
        public String name() {
            return "blue circle";
        }
    }

    /**
     * A qualified service.
     */
    @Green
    @Service.Singleton
    static class GreenCircle implements Circle {

        @Override
        public String name() {
            return "green circle";
        }
    }

    /**
     * A service that injects using the custom qualifier.
     *
     * @param circle circle
     */
    @Service.Singleton
    record BlueDrawing(@Blue Circle circle) {
    }

    /**
     * A service that injects using the custom qualifier.
     *
     * @param circle circle
     */
    @Service.Singleton
    record GreenDrawing(@Green Circle circle) {
    }
}
