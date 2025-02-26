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
 * An example that illustrates usages of {@link Service.Named}.
 */
class NamedExample {

    private NamedExample() {
    }

    public static void main(String[] args) {
        var blueCircle = Services.get(BlueCircle.class);
        var greenCircle = Services.get(GreenCircle.class);

        System.out.printf("blue circle color hex-code: %s%n", blueCircle.color().hexCode());
        System.out.printf("green circle color hex-code: %s%n", greenCircle.color().hexCode());
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
    @Service.Named("blue")
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
    @Service.Named("green")
    @Service.Singleton
    static class Green implements Color {

        @Override
        public String hexCode() {
            return "008000";
        }
    }

    /**
     * A service that qualifies the injection point using {@link Service.Named}.
     *
     * @param color color
     */
    @Service.Singleton
    record BlueCircle(@Service.Named("blue") Color color) {
    }

    /**
     * A service that qualifies the injection point using {@link Service.Named}.
     *
     * @param color color
     */
    @Service.Singleton
    record GreenCircle(@Service.Named("green") Color color) {
    }
}
