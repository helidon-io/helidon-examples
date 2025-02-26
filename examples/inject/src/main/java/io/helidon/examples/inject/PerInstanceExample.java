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
 * An example that illustrates usages of {@link Service.PerInstance}.
 */
class PerInstanceExample {

    private PerInstanceExample() {
    }

    public static void main(String[] args) {
        var circles = Services.get(Circles.class);

        System.out.printf("blue circle name: %s%n", circles.blue().name());
        System.out.printf("blue circle color hex-code: %s%n", circles.blue().color().hexCode());
        System.out.printf("green circle name: %s%n", circles.green().name());
        System.out.printf("green circle color hex-code: %s%n", circles.green().color().hexCode());
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
     * A service that is created for each named instance of {@link Color}.
     *
     * @param name  the matched name
     * @param color the matched color
     */
    @Service.PerInstance(Color.class)
    record Circle(@Service.InstanceName String name, Color color) {
    }

    /**
     * A service that illustrates the inherited names.
     *
     * @param blue  blue circle
     * @param green green circle
     */
    @Service.Singleton
    record Circles(@Service.Named("blue") Circle blue,
                   @Service.Named("green") Circle green) {
    }
}
