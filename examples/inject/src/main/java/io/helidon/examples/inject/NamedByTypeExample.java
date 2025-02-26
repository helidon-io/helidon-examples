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
 * An example that illustrates usages of {@link Service.NamedByType}.
 */
class NamedByTypeExample {

    private NamedByTypeExample() {
    }

    public static void main(String[] args) {
        var blueCircle = Services.get(BlueSquare.class);
        var greenCircle = Services.get(GreenSquare.class);

        System.out.printf("blue square color hex-code: %s%n", blueCircle.color().hexCode());
        System.out.printf("green square color hex-code: %s%n", greenCircle.color().hexCode());
    }

    /**
     * A service to be implemented by named services.
     */
    interface Color {
        String hexCode();
    }

    /**
     * A named service.
     */
    @Service.NamedByType(Blue.class)
    @Service.Singleton
    static class Blue implements Color {

        @Override
        public String hexCode() {
            return "0000FF";
        }
    }

    /**
     * A named service.
     */
    @Service.NamedByType(Green.class)
    @Service.Singleton
    static class Green implements Color {

        @Override
        public String hexCode() {
            return "008000";
        }
    }

    /**
     * A service that qualifies the injection point using {@link Service.NamedByType}.
     *
     * @param color color
     */
    @Service.Singleton
    record BlueSquare(@Service.NamedByType(Blue.class) Color color) {
    }

    /**
     * A service that qualifies the injection point using {@link Service.NamedByType}.
     *
     * @param color color
     */
    @Service.Singleton
    record GreenSquare(@Service.NamedByType(Green.class) Color color) {
    }
}
