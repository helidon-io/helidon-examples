/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
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
package io.helidon.examples.integrations.oci.adbs;

import java.util.function.Supplier;

import io.helidon.service.registry.Service.Inject;
import io.helidon.service.registry.Service.Singleton;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.database.Database;

import static java.util.Objects.requireNonNull;

import static com.oracle.bmc.database.DatabaseClient.builder;

@Singleton
final class DatabaseFactory implements Supplier<Database> {

    private final BasicAuthenticationDetailsProvider adp;

    @Inject
    DatabaseFactory(BasicAuthenticationDetailsProvider adp) {
        super();
        this.adp = requireNonNull(adp, "adp");
    }

    @Override // Supplier<Database>
    public Database get() {
        return builder().build(this.adp);
    }

}
