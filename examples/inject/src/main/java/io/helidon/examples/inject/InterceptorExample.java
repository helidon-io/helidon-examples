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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.helidon.service.registry.Interception;
import io.helidon.service.registry.InterceptionContext;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates usages of {@link Interception.Interceptor}.
 */
class InterceptorExample {

    private InterceptorExample() {
    }

    public static void main(String[] args) {
        var myService = Services.get(MyConcreteService.class);
        var myIFaceContract = Services.get(MyContract.class);
        var myAbstractClassContract = Services.get(MyAbstractClassContract.class);
        var myIFaceProvidedContract = Services.get(MyOtherContract.class);
        var myAbstractClassProvidedContract = Services.get(MyOtherAbstractClassContract.class);

        System.out.println(myService.sayHello("Joe"));
        System.out.println(myService.sayHello("Jack"));
        System.out.println(myIFaceContract.sayHello("Julia"));
        System.out.println(myIFaceContract.sayHello("Jeanne"));
        System.out.println(myAbstractClassContract.sayHello("Jessica"));
        System.out.println(myAbstractClassContract.sayHello("Juliet"));
        System.out.println(myIFaceProvidedContract.sayHello("Jennifer"));
        System.out.println(myIFaceProvidedContract.sayHello("Josephine"));
        System.out.println(myAbstractClassProvidedContract.sayHello("Joceline"));
        System.out.println(myAbstractClassProvidedContract.sayHello("Jacqueline"));
        MyServiceInterceptor.INVOKED.forEach(System.out::println);
    }

    /**
     * An annotation to mark methods to be intercepted.
     */
    @Interception.Intercepted
    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Traced {
    }

    /**
     * A contract with an intercepted method.
     */
    interface MyContract {

        @Traced
        String sayHello(String name);
    }

    /**
     * An abstract class contract with an intercepted method.
     */
    @Service.Contract
    abstract static class MyAbstractClassContract {

        @Traced
        abstract String sayHello(String name);

        @Traced
        String sayHelloDirect(String name) {
            return "Hello %s!".formatted(name);
        }
    }

    /**
     * A delegate-able abstract class contract with an intercepted method.
     */
    @Interception.Delegate
    abstract static class MyOtherAbstractClassContract {

        @Traced
        abstract String sayHello(String name);
    }

    /**
     * Another contract with an intercepted method.
     */
    interface MyOtherContract {

        @Traced
        String sayHello(String name);
    }

    /**
     * An interceptor implementation that supports {@link Traced}.
     */
    @Service.Singleton
    @Service.NamedByType(Traced.class)
    static class MyServiceInterceptor implements Interception.Interceptor {
        static final List<String> INVOKED = new ArrayList<>();

        @Override
        public <V> V proceed(InterceptionContext ctx, Chain<V> chain, Object... args) throws Exception {
            INVOKED.add("%s.%s: %s".formatted(
                    ctx.serviceInfo().serviceType().declaredName(),
                    ctx.elementInfo().elementName(),
                    Arrays.asList(args)));
            return chain.proceed(args);
        }
    }

    /**
     * A singleton service with an intercepted constructor and an intercepted method.
     */
    @Service.Singleton
    static class MyConcreteService {

        @Traced
        MyConcreteService() {
        }

        @Traced
        String sayHello(String name) {
            return "Hello %s!".formatted(name);
        }
    }

    /**
     * A service that implements a contract with intercepted methods.
     */
    @Service.Singleton
    static class MyContractImpl implements MyContract {

        @Override
        public String sayHello(String name) {
            return "Hello %s!".formatted(name);
        }
    }

    /**
     * A service that extends an abstract class contract with an intercepted method.
     */
    @Service.Singleton
    static class MyAbstractClassContractImpl extends MyAbstractClassContract {

        @Override
        public String sayHello(String name) {
            return "Hello %s!".formatted(name);
        }
    }

    /**
     * A service that implements a provider of a contract with an intercepted method.
     */
    @Service.Singleton
    static class MyContractProvider implements Supplier<MyOtherContract> {
        @Override
        public MyOtherContract get() {
            return "Hello %s!"::formatted;
        }
    }

    /**
     * A provider of an intercepted abstract contract.
     */
    @Service.Singleton
    static class MyAbstractContractProvider implements Supplier<MyOtherAbstractClassContract> {

        @Override
        public MyOtherAbstractClassContract get() {
            return new MyOtherAbstractClassContract() {

                @Override
                String sayHello(String name) {
                    return "Hello %s!".formatted(name);
                }
            };
        }
    }
}
