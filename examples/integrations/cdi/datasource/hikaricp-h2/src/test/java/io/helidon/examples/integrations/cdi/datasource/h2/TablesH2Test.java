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

package io.helidon.examples.integrations.cdi.datasource.h2;

import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.ws.rs.client.WebTarget;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@HelidonTest
class TablesH2Test {

    @Test
    void testGet(WebTarget target) {
        try (var rsp = target.path("/tables").request().get()) {
            assertThat(rsp.getStatus(), is(200));
            String entity = rsp.readEntity(String.class);
            assertThat(entity.lines().toList(), hasItems("TABLES", "COLUMNS", "VIEWS"));
        }
    }
}
