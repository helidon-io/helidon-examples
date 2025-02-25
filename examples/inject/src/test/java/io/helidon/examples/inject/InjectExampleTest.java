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
import java.util.Map;

import io.helidon.service.registry.ServiceRegistryConfig;
import io.helidon.service.registry.ServiceRegistryManager;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.Scope;
import io.helidon.service.registry.Scopes;
import io.helidon.service.registry.Services;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class InjectExampleTest {

    @Test
    void testDescribe() {
        var injectConfig = ServiceRegistryConfig.builder()
                .putContractInstance(DescribeExample.MyContract.class, new DescribeExample.MyContractImpl())
                .build();
        var manager = ServiceRegistryManager.create(injectConfig);
        try {
            var myContract = manager.registry().get(DescribeExample.MyContract.class);
            assertThat(myContract.sayHello(), is("Hello World!"));
        } finally {
            manager.shutdown();
        }
    }

    @Test
    void testInterceptor() {
        var myConcreteService = Services.get(InterceptorExample.MyConcreteService.class);
        var myContract = Services.get(InterceptorExample.MyContract.class);
        var myAbstractClassContract = Services.get(InterceptorExample.MyAbstractClassContract.class);
        var myProvidedContract = Services.get(InterceptorExample.MyOtherContract.class);
        var myAbstractClassProvidedContract = Services.get(InterceptorExample.MyOtherAbstractClassContract.class);

        assertThat(myConcreteService.sayHello("Joe"), is("Hello Joe!"));
        assertThat(myConcreteService.sayHello("John"), is("Hello John!"));
        assertThat(myContract.sayHello("Julia"), is("Hello Julia!"));
        assertThat(myContract.sayHello("Jeanne"), is("Hello Jeanne!"));
        assertThat(myAbstractClassContract.sayHello("Jessica"), is("Hello Jessica!"));
        assertThat(myAbstractClassContract.sayHello("Juliet"), is("Hello Juliet!"));
        assertThat(myProvidedContract.sayHello("Jennifer"), is("Hello Jennifer!"));
        assertThat(myProvidedContract.sayHello("Josephine"), is("Hello Josephine!"));
        assertThat(myAbstractClassContract.sayHelloDirect("John"), is("Hello John!"));
        assertThat(myAbstractClassProvidedContract.sayHello("Joceline"), is("Hello Joceline!"));
        assertThat(myAbstractClassProvidedContract.sayHello("Jacqueline"), is("Hello Jacqueline!"));
        assertThat(InterceptorExample.MyServiceInterceptor.INVOKED, is(List.of(
                "%s.<init>: []".formatted(InterceptorExample.MyConcreteService.class.getName()),
                "%s.sayHello: [Joe]".formatted(InterceptorExample.MyConcreteService.class.getName()),
                "%s.sayHello: [John]".formatted(InterceptorExample.MyConcreteService.class.getName()),
                "%s.sayHello: [Julia]".formatted(InterceptorExample.MyContractImpl.class.getName()),
                "%s.sayHello: [Jeanne]".formatted(InterceptorExample.MyContractImpl.class.getName()),
                "%s.sayHello: [Jessica]".formatted(InterceptorExample.MyAbstractClassContractImpl.class.getName()),
                "%s.sayHello: [Juliet]".formatted(InterceptorExample.MyAbstractClassContractImpl.class.getName()),
                "%s.sayHello: [Jennifer]".formatted(InterceptorExample.MyContractProvider.class.getName()),
                "%s.sayHello: [Josephine]".formatted(InterceptorExample.MyContractProvider.class.getName()),
                "%s.sayHelloDirect: [John]".formatted(InterceptorExample.MyAbstractClassContractImpl.class.getName()),
                "%s.sayHello: [Joceline]".formatted(InterceptorExample.MyAbstractContractProvider.class.getName()),
                "%s.sayHello: [Jacqueline]".formatted(InterceptorExample.MyAbstractContractProvider.class.getName()))));
    }

    @Test
    void testNamedByType() {
        var blueCircle = Services.get(NamedByTypeExample.BlueSquare.class);
        var greenCircle = Services.get(NamedByTypeExample.GreenSquare.class);

        assertThat(blueCircle.color().hexCode(), is("0000FF"));
        assertThat(greenCircle.color().hexCode(), is("008000"));
    }

    @Test
    void testNamed() {
        var blueCircle = Services.get(NamedExample.BlueCircle.class);
        var greenCircle = Services.get(NamedExample.GreenCircle.class);

        assertThat(blueCircle.color().hexCode(), is("0000FF"));
        assertThat(greenCircle.color().hexCode(), is("008000"));
    }

    @Test
    void testWeighted() {
        var color = Services.get(WeightedExample.Color.class);

        assertThat(color.name(), is("green"));
    }

    @Test
    void testPerInstance() {
        var circles = Services.get(PerInstanceExample.Circles.class);

        assertThat(circles.blue().name(), is("blue"));
        assertThat(circles.blue().color().hexCode(), is("0000FF"));
        assertThat(circles.green().name(), is("green"));
        assertThat(circles.green().color().hexCode(), is("008000"));
    }

    @Test
    void testPerLookup() {
        var myInstance1 = Services.get(PerLookupExample.MyInstance.class);
        var myInstance2 = Services.get(PerLookupExample.MyInstance.class);
        var mySingleton = Services.get(PerLookupExample.MySingleton.class);

        assertThat(System.identityHashCode(myInstance1),
                is(not(System.identityHashCode(myInstance2))));

        assertThat(System.identityHashCode(mySingleton.instance().get()),
                is(not(System.identityHashCode(mySingleton.instance().get()))));
    }

    @Test
    void testRequestScope() {
        var myService = Services.get(PerRequestExample.MyService.class);
        var scopes = Services.get(Scopes.class);

        try (Scope ignored = scopes.createScope(Service.PerRequest.TYPE, "test-1", Map.of())) {
            assertThat(myService.contract().get().sayHello(), is("Hello World!"));
        }
    }

    @Test
    void testCustomScope() {
        var myService = Services.get(CustomScopeExample.MyService.class);
        var scopes = Services.get(Scopes.class);

        try (Scope ignored = scopes.createScope(CustomScopeExample.MyScope.TYPE, "test-1", Map.of())) {
            assertThat(myService.contract().get().sayHello(), is("Hello World!"));
        }
    }

    @Test
    void testInjectionPoints() {
        var greetings = Services.get(InjectionPointsExample.Greetings.class);

        assertThat(greetings.greet(), containsInAnyOrder(
                "%s: Hello Joe!".formatted(InjectionPointsExample.GreetingWithCyclicDep1.class.getSimpleName()),
                "%s: Hello Jack!".formatted(InjectionPointsExample.GreetingWithCyclicDep1.class.getSimpleName()),
                "%s: Hello Julia!".formatted(InjectionPointsExample.GreetingWithExplicitCtorInjection.class.getSimpleName()),
                "%s: Hello Jeanne!".formatted(InjectionPointsExample.GreetingWithFieldInjection.class.getSimpleName()),
                "%s: Hello Jessica!".formatted(InjectionPointsExample.GreetingWithImplicitCtorInjection.class.getSimpleName()),
                "%s: Hello Juliet!".formatted(InjectionPointsExample.GreetingWithInheritedFieldInjection.class.getSimpleName()),
                "%s: Hello Jennifer!".formatted(InjectionPointsExample.GreetingWithMethodInjection.class.getSimpleName()),
                "%s: Hello Josephine!".formatted(InjectionPointsExample.GreetingWithOptionalIp.class.getSimpleName()),
                "%s: Hello John!".formatted(InjectionPointsExample.GreetingWithRecord.class.getSimpleName()),
                "%s: Hello Jacqueline!".formatted(InjectionPointsExample.GreetingWithRecordCanonicalCtor.class.getSimpleName())
                        .toUpperCase(),
                "%s: Hello Joe!".formatted(InjectionPointsExample.GreetingWithRecordCompactCtor.class.getSimpleName()),
                "%s: Hello Joe!!!".formatted(InjectionPointsExample.GreetingWithRecordCustomCtor.class.getSimpleName())
        ));
    }

    @Test
    void testExternalContract() {
        var nameGenerator = Services.get(ExternalContractExample.NameGenerator.class);
        assertThat(ExternalContractExample.RandomNameGenerator.NAMES, hasItem(nameGenerator.name()));
    }

    @Test
    void testRunLevel() {
        var manager = ServiceRegistryManager.create();
        try {
            RunLevelExample.startRunLevels(manager.registry());
            assertThat(RunLevelExample.STARTUP_EVENTS, hasItems("level1", "level2"));
        } finally {
            manager.shutdown();
        }
    }

    @Test
    void testGenerics() {
        var myService = Services.get(GenericsExample.MyService.class);

        assertThat(myService.blueCircle().name(), is("blue circle"));
        assertThat(myService.greenCircle().name(), is("green circle"));
        assertThat(myService.circleNames(), is(List.of("blue circle", "green circle")));
    }

    @Test
    void testCovariance() {
        var shelter = Services.get(CovarianceExample.Shelter.class);

        var all = shelter.all().stream().map(CovarianceExample.Pet::name).toList();
        assertThat(all, is(List.of("Bengal", "Boxer", "Husky", "Siamese")));

        var cats = shelter.cats().stream().map(CovarianceExample.Cat::name).toList();
        assertThat(cats, is(List.of("Bengal", "Siamese")));

        var dogs = shelter.dogs().stream().map(CovarianceExample.Dog::name).toList();
        assertThat(dogs, is(List.of("Boxer", "Husky")));
    }

    @Test
    void testEvents() {
        var myEmitter = Services.get(EventsExample.MyEmitter.class);
        var myObserver = Services.get(EventsExample.MyObserver.class);
        var myIdEmitter = Services.get(EventsExample.MyIdEmitter.class);
        var myIdObserver = Services.get(EventsExample.MyIdObserver.class);
        var myNameEmitter = Services.get(EventsExample.MyNameEmitter.class);
        var myNameObserver = Services.get(EventsExample.MyNameObserver.class);

        myEmitter.emit("foo");
        myEmitter.emit("bar");
        assertThat(myObserver.messages(), is(List.of("foo", "bar")));

        myIdEmitter.emit("123");
        myIdEmitter.emit("456");
        assertThat(myIdObserver.ids(), is(List.of("123", "456")));

        myNameEmitter.emit("Jack");
        myNameEmitter.emit("Jill");
        assertThat(myNameObserver.names(), is(List.of("Jack", "Jill")));
    }

    @Test
    void testFactories() {
        var myService = Services.get(FactoryExample.MyService.class);
        var colors = Services.get(FactoryExample.Colors.class);
        var systemInfo = Services.get(FactoryExample.SystemInfo.class);

        System.out.printf("%s%n", myService);
        System.out.printf("%s%n", colors);
        System.out.printf("%s%n", systemInfo);
    }
}
