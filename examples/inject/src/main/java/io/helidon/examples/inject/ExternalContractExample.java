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
import java.util.Random;

import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;

/**
 * An example that illustrates usages of {@link Service.ExternalContracts}.
 */
class ExternalContractExample {

    private ExternalContractExample() {
    }

    /**
     * A service that implements {@link CharSequence} contract.
     */
    @Service.PerLookup
    @Service.ExternalContracts(CharSequence.class)
    static class RandomName implements CharSequence {

        static final List<String> NAMES = List.of(
                "Joe", "Jack", "Julia", "Jeanne", "Jessica",
                "Juliet", "Jennifer", "Josephine");

        private static final Random RANDOM = new Random();

        private final String name = NAMES.get(RANDOM.nextInt(0, NAMES.size() - 1));

        @Override
        public int length() {
            return name.length();
        }

        @Override
        public char charAt(int index) {
            return name.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return name.subSequence(start, end);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        var registry = ServiceRegistryManager.create().registry();
        var name = registry.get(CharSequence.class);
        System.out.println(name);
    }
}
