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
import java.util.Optional;
import java.util.function.Supplier;

import io.helidon.common.GenericType;
import io.helidon.service.registry.Lookup;
import io.helidon.service.registry.Qualifier;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates the various factories.
 */
class FactoryExample {

    private FactoryExample() {
    }

    public static void main(String[] args) {
        var myService = Services.get(MyService.class);
        var colors = Services.get(Colors.class);
        var systemInfo = Services.get(SystemInfo.class);

        System.out.printf("%s%n", myService);
        System.out.printf("%s%n", colors);
        System.out.printf("%s%n", systemInfo);
    }

    /**
     * A service that is provided by {@link MyServiceFactory}.
     */
    record MyService(String name) {
    }

    /**
     * A factory that provides {@link MyService}.
     */
    @Service.Singleton
    record MyServiceFactory() implements Supplier<MyService> {

        @Override
        public MyService get() {
            return new MyService("Joe");
        }
    }

    /**
     * A custom qualifier.
     */
    @Service.Qualifier
    @interface Red {
    }

    /**
     * A custom qualifier.
     */
    @Service.Qualifier
    @interface Green {
    }

    /**
     * A custom qualifier.
     */
    @Service.Qualifier
    @interface Blue {
    }

    /**
     * A service that is provided by {@link MyBulkServiceFactory}.
     */
    record Color(String name) {
    }

    /**
     * A bulk factory that provides qualified {@link Color} instances.
     */
    @Service.Singleton
    record MyBulkServiceFactory() implements Service.ServicesFactory<Color> {

        static final Qualifier RED_QUALIFIER = Qualifier.create(Red.class);
        static final Qualifier GREEN_QUALIFIER = Qualifier.create(Green.class);
        static final Qualifier BLUE_QUALIFIER = Qualifier.create(Blue.class);

        @Override
        public List<Service.QualifiedInstance<Color>> services() {
            return List.of(
                    Service.QualifiedInstance.create(new Color("red"), RED_QUALIFIER),
                    Service.QualifiedInstance.create(new Color("green"), GREEN_QUALIFIER),
                    Service.QualifiedInstance.create(new Color("blue"), BLUE_QUALIFIER));
        }
    }

    /**
     * A service that injects qualified colors.
     *
     * @param red   red
     * @param green green
     * @param blue  blue
     */
    @Service.Singleton
    record Colors(@Red Color red, @Green Color green, @Blue Color blue) {
    }

    /**
     * A custom qualifier used as an injection point.
     */
    @Service.Qualifier
    @interface SystemProperty {
        String value();
    }

    /**
     * A service with a custom injection point.
     *
     * @param javaVersion {@code java.version} system property value
     */
    @Service.Singleton
    record SystemInfo(@SystemProperty("java.version") String javaVersion) {
    }

    /**
     * A qualified factory that supports {@link SystemProperty}.
     */
    @Service.Singleton
    record InjectionPointFactoryWithQualifier() implements Service.QualifiedFactory<String, SystemProperty> {

        @Override
        public Optional<Service.QualifiedInstance<String>> first(Qualifier qualifier,
                                                                 Lookup lookup,
                                                                 GenericType genericType) {
            return qualifier.value()
                    .flatMap(name -> Optional.ofNullable(System.getProperty(name)))
                    .map(value -> Service.QualifiedInstance.create(value, qualifier));
        }
    }
}
