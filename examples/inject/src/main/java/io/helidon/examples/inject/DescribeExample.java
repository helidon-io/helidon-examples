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
import io.helidon.service.registry.ServiceRegistryConfig;
import io.helidon.service.registry.ServiceRegistryManager;

/**
 * An example that illustrates usages of {@link Service.Describe}.
 */
class DescribeExample {

    private DescribeExample() {
    }

    public static void main(String[] args) {
        var injectConfig = ServiceRegistryConfig.builder()
                // pass the non managed instance of the described contract
                .putContractInstance(MyContract.class, new MyContractImpl())
                .build();

        var manager = ServiceRegistryManager.start(injectConfig);
        try {
            var myContract = manager.registry().get(MyContract.class);
            System.out.println(myContract.sayHello());
        } finally {
            manager.shutdown();
        }
    }

    /**
     * A service that needs to be described separately.
     */
    @Service.Describe
    @Service.Contract
    interface MyContract {
        String sayHello();
    }

    /**
     * A non-service implementation of the contract.
     * It is instantiated manually and passed to the registry manager config.
     */
    static class MyContractImpl implements MyContract {

        @Override
        public String sayHello() {
            return "Hello World!";
        }
    }

    /**
     * A singleton service that injects the described contract.
     *
     * @param myContract myContract
     */
    @Service.Singleton
    record MyService(MyContract myContract) {
    }
}
