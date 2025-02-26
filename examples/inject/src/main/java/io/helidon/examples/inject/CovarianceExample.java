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
 * An example that demonstrates covariant lookups.
 */
class CovarianceExample {

    private CovarianceExample() {
    }

    public static void main(String[] args) {
        var shelter = Services.get(Shelter.class);

        System.out.println("All pets:");
        shelter.all.stream().map(Pet::name).forEach(System.out::println);

        System.out.println("\nAll cats:");
        shelter.cats.stream().map(Cat::name).forEach(System.out::println);

        System.out.println("\nAll dogs:");
        shelter.dogs.stream().map(Dog::name).forEach(System.out::println);
    }

    sealed interface Pet permits Cat, Dog {
        default String name() {
            return getClass().getSimpleName();
        }
    }

    sealed interface Cat extends Pet permits Siamese, Bengal {
    }

    sealed interface Dog extends Pet permits Boxer, Husky {
    }

    @Service.Singleton
    record Siamese() implements Cat {
    }

    @Service.Singleton
    record Bengal() implements Cat {
    }

    @Service.Singleton
    record Boxer() implements Dog {
    }

    @Service.Singleton
    record Husky() implements Dog {
    }

    @Service.Singleton
    record Shelter(List<Pet> all, List<Cat> cats, List<Dog> dogs) {
    }
}
