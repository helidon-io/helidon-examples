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
package io.helidon.examples.integrations.oci.adbs.iam;

import java.sql.SQLException;

import javax.sql.DataSource;

import io.helidon.service.registry.Services;

import static io.helidon.logging.common.LogConfig.configureRuntime;

/**
 * This example's main class demonstrating Helidon's ability to access an <a
 * href="https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/index.html">Oracle Autonomous AI Database
 * Serverless</a> instance running in the <a href="https://cloud.oracle.com/">Oracle Cloud</a>, using <a
 * href="https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#access-token-provider">token-based
 * authentication</a>.
 *
 * @see #main(String[])
 */
public final class Main {

    private Main() {
        super();
    }

    /**
     * Runs this example.
     *
     * <p>This example connects to an Oracle Autonomous AI Database Serverless instance running in Oracle Cloud that you
     * must have provisioned and configured beforehand (see {@code README.md} in the top-level directory of this example
     * for more details concerning Oracle Cloud requirements).</p>
     *
     * @param args command line arguments; ignored by this example
     * @throws SQLException if a database error occurs
     */
    public static void main(String[] args) throws SQLException {
        // For this example, tell Helidon to set up logging. See ../../../../../../../resources/logging.properties.
        configureRuntime();

        // Use the service registry to get a DataSource that has been comfigured in (in this example)
        // ../../../../../../../../resources/application.yaml.
        try (var c = Services.get(DataSource.class).getConnection();
             var s = c.createStatement();
             var rs = s.executeQuery("SELECT 'Hello, world!';");) {
            rs.next();
            System.out.println(rs.getString(1));
        }
    }

}
