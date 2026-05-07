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

import java.sql.SQLException;

import javax.sql.DataSource;

import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;

/**
 * This example's main class demonstrating Helidon's ability to access an <a
 * href="https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/index.html">Oracle Autonomous AI Database
 * Serverless</a> instance running in the <a href="https://cloud.oracle.com/">Oracle Cloud</a>.
 *
 * @see #main(String[])
 * @see AdbsDataSource
 */
public final class Main {

    private Main() {
        super();
    }

    /**
     * Runs this example.
     *
     * <p>This example connects to an Oracle Autonomous AI Database Serverless instance running in Oracle Cloud that you
     * must have provisioned beforehand (see {@code README.md} in the top-level directory of this example for more
     * details concerning Oracle Cloud requirements).</p>
     *
     * @param args command line arguments; ignored by this example
     * @throws SQLException if an error occurs
     * @see AdbsDataSource
     */
    public static void main(String[] args) throws SQLException {
        // For this example, tell Helidon to set up logging. See ../../../../../../../resources/logging.properties.
        LogConfig.configureRuntime();

        // Get the DataSource provided by Helidon Data. The DataSource will be an instance of
        // oracle.ucp.jdbc.PoolDataSource, which is a class providing connection pooling supplied by the Oracle
        // Universal Connection Pool, because this example's configuration specifies use of the UCP.
        //
        // Its underlying "connection factory", i.e. the DataSource that actually supplies physical connections, will be
        // an instance of io.helidon.examples.integrations.oci.adbs.AdbsDataSource. This is also specified in the
        // configuration. See ../../../../../../../resources/application.yaml. See ./AdbsDataSource.java.
        var ds = Services.get(DataSource.class);

        // Use the DataSource to connect to the database. Note that wallet download and inspection happens
        // automatically, and the database username and password are sourced from the vault. See
        // ../../../../../../../resources/meta-config.yaml and ../../../../../../../resources/application.yaml.
        //
        // This first connection acquisition will take some time as the wallet is downloaded and secrets are read. You
        // can see what is happening by adjusting the log levels in ../../../../../../../resources/logging.properties
        // and re-building the program.
        try (var c = ds.getConnection();
             var s = c.createStatement();
             var rs = s.executeQuery("SELECT 'Hello, ';")) {
            while (rs.next()) {
                System.out.print(rs.getString(1));
            }
        }

        // This next connection acquisition will be very fast because all the other work has been done already.
        try (var c = ds.getConnection();
             var s = c.createStatement();
             var rs = s.executeQuery("SELECT 'world!';")) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }

}
