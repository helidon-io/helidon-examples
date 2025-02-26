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

import java.util.List;

import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that demonstrates using generics.
 */
class GenericsExample {

    private GenericsExample() {
    }

    public static void main(String[] args) {
        var myService = Services.get(MyService.class);

        System.out.println(myService.blueCircle().name());
        System.out.println(myService.greenCircle().name());
        myService.circleNames().forEach(System.out::println);
    }

    /**
     * A service to be implemented by qualified services.
     */
    interface Color {
        String name();
    }

    @Service.Singleton
    static class Blue implements Color {
        @Override
        public String name() {
            return "blue";
        }
    }

    @Service.Singleton
    static class Green implements Color {
        @Override
        public String name() {
            return "green";
        }
    }

    interface Circle<T extends Color> {
        T color();

        default String name() {
            return color().name() + " circle";
        }
    }

    @Service.Singleton
    record BlueCircle(Blue color) implements Circle<Blue> {
    }

    @Service.Singleton
    record GreenCircle(Green color) implements Circle<Green> {
    }

    @Service.Singleton
    record MyService(Circle<Blue> blueCircle,
                     Circle<Green> greenCircle,
                     List<Circle<Color>> circles) {

        List<String> circleNames() {
            return circles.stream()
                    .map(GenericsExample.Circle::name)
                    .toList();
        }
    }
}
