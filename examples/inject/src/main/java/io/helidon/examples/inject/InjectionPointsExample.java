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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates usages of {@link Service.Inject}.
 */
class InjectionPointsExample {

    private InjectionPointsExample() {
    }

    public static void main(String[] args) {
        var greetings = Services.get(Greetings.class);

        greetings.greet().forEach(System.out::println);
    }

    /**
     * A service to be injected.
     */
    @Service.PerLookup
    static class Greeter {

        private Function<String, String> filter = Function.identity();

        Greeter filter(Function<String, String> filter) {
            this.filter = filter;
            return this;
        }

        String greet(String prefix, String name) {
            return filter.apply("%s: Hello %s!".formatted(prefix, name));
        }
    }

    /**
     * A service to be implemented by all variations.
     */
    interface Greeting {

        Greeter greeter();

        default String greet(String name) {
            return greeter().greet(getClass().getSimpleName(), name);
        }
    }

    /**
     * A service that uses constructor Service.
     * There is only one constructor, thus {@link Service.Inject} is optional.
     */
    @Service.Singleton
    static class GreetingWithImplicitCtorInjection implements Greeting {

        private final Greeter greeter;

        GreetingWithImplicitCtorInjection(Greeter greeter) {
            this.greeter = greeter;
        }

        @Override
        public Greeter greeter() {
            return greeter;
        }
    }

    /**
     * A service that uses constructor injection and that has multiple constructors.
     * {@link Service.Inject} is required.
     */
    @Service.Singleton
    static class GreetingWithExplicitCtorInjection implements Greeting {

        private final Greeter greeter;

        @Service.Inject
        GreetingWithExplicitCtorInjection(Greeter greeter) {
            this.greeter = greeter;
        }

        @SuppressWarnings("unused")
        GreetingWithExplicitCtorInjection() {
            this(new Greeter());
        }

        @Override
        public Greeter greeter() {
            return greeter;
        }
    }

    /**
     * A service that uses field Service.
     * The visibility of the field must be at minimum package private.
     */
    @Service.Singleton
    static class GreetingWithFieldInjection implements Greeting {

        @Service.Inject
        @SuppressWarnings("checkstyle:VisibilityModifier")
        protected Greeter greeter;

        @Override
        public Greeter greeter() {
            return greeter;
        }
    }

    /**
     * A service that uses method Service.
     * The visibility of the method must be at minimum package private.
     */
    @Service.Singleton
    static class GreetingWithMethodInjection implements Greeting {

        private Greeter greeter;

        @Service.Inject
        void setGreeter(Greeter greeter) {
            this.greeter = greeter;
        }

        @Override
        public Greeter greeter() {
            return greeter;
        }
    }

    /**
     * A service that extends a base class with field Service.
     */
    @Service.Singleton
    static class GreetingWithInheritedFieldInjection extends GreetingWithFieldInjection {
    }

    /**
     * A service as a record with constructor Service.
     *
     * @param greeter greeter
     */
    @Service.Singleton
    record GreetingWithRecord(Greeter greeter) implements Greeting {
    }

    /**
     * A service as a record with a compact constructor.
     * {@link Service.Inject} is optional.
     *
     * @param greeter greeter
     */
    @Service.Singleton
    record GreetingWithRecordCompactCtor(Greeter greeter) implements Greeting {

        @Service.Inject
        GreetingWithRecordCompactCtor {
        }
    }

    /**
     * A service as a record with a canonical constructor.
     * The greeter filter is updated to illustrate the need for a canonical constructor.
     *
     * @param greeter greeter
     */
    @Service.Singleton
    record GreetingWithRecordCanonicalCtor(Greeter greeter) implements Greeting {

        @Service.Inject
        GreetingWithRecordCanonicalCtor(Greeter greeter) {
            this.greeter = greeter.filter(String::toUpperCase);
        }
    }

    /**
     * A service as a record with a custom constructor.
     * {@link Service.Inject} is required.
     *
     * @param greeter greeter
     * @param suffix  a suffix to illustrate the need for a custom constructor
     */
    @Service.Singleton
    record GreetingWithRecordCustomCtor(Greeter greeter, String suffix) implements Greeting {

        @Service.Inject
        GreetingWithRecordCustomCtor(Greeter greeter) {
            this(greeter, "!!");
        }

        @Override
        public String greet(String name) {
            return Greeting.super.greet(name) + suffix;
        }
    }

    /**
     * A service that uses an {@link Optional optional} as an injection point.
     * A non-existing qualifier is used to illustrate what happens when the resolution fails.
     *
     * @param optionalGreeter optional greeter
     */
    @Service.Singleton
    record GreetingWithOptionalIp(@Service.Named("non-existing") Optional<Greeter> optionalGreeter) implements Greeting {

        @Override
        public Greeter greeter() {
            return optionalGreeter.orElseGet(Greeter::new);
        }
    }

    /**
     * A service that uses a {@link List list} as an injection point.
     *
     * @param greetings all the resolved greetings
     */
    @Service.Singleton
    record Greetings(List<Greeting> greetings) {

        public static final List<String> NAMES = List.of(
                "Joe", "Jack", "Julia", "Jeanne", "Jessica",
                "Juliet", "Jennifer", "Josephine", "John", "Jacqueline");

        List<String> greet() {
            var result = new ArrayList<String>();
            var it = greetings.listIterator();
            var index = 0;
            while (it.hasNext()) {
                if (it.nextIndex() >= NAMES.size()) {
                    index = 0;
                }
                result.add(it.next().greet(NAMES.get(index++)));
            }
            return result;
        }
    }

    /**
     * A service with a cyclic dependency that is broken-up using a {@link Supplier}.
     *
     * @param dependency dependency
     */
    @Service.Singleton
    record GreetingWithCyclicDep1(Supplier<GreetingWithCyclicDep2> dependency) implements Greeting {

        @Override
        public Greeter greeter() {
            return dependency.get().greeter();
        }
    }

    /**
     * A service with a cyclic dependency.
     *
     * @param dependency dependency
     * @param greeter    greeter
     */
    @Service.Singleton
    record GreetingWithCyclicDep2(GreetingWithCyclicDep1 dependency, Greeter greeter) implements Greeting {

        @Override
        public String greet(String name) {
            return dependency.greet(name);
        }
    }
}
