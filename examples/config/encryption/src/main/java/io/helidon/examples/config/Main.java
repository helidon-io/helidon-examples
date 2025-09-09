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
package io.helidon.examples.config;

import io.helidon.config.Config;

/**
 * The application main class.
 */
public class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // initialize config from default configuration
        Config config = Config.global();

        System.out.println("SECURE_CONFIG_AES_MASTER_PWD=" + System.getenv("SECURE_CONFIG_AES_MASTER_PWD"));
        System.out.println("SECRET!!! secret-key=" + config.get("secret-key").asString().get());
    }
}
