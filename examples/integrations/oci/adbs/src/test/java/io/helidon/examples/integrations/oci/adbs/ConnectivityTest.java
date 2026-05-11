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

import io.helidon.config.Config;
import io.helidon.service.registry.Services;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A JUnit Jupiter test suite that exercises the {@link AdbsDataSource} class.
 *
 * <p>For user convenience and example simplicitly, tests in this suite use JUnit "assumptions", which allow for tests
 * to be simply skipped if required information is missing. For the required information, see the {@code README.md} file
 * in the top-level directory of this example. For more information about JUnit assumptions, ee {@link
 * org.junit.jupiter.api.Assumptions}.</p>
 *
 * @see #testConnectivity()
 * @see org.junit.jupiter.api.Assumptions
 */
final class ConnectivityTest {

    private ConnectivityTest() {
        super();
    }

    /**
     * Tests the {@link AdbsDataSource} class to ensure that it can connect to an Oracle Autonomous AI Database
     * Serverless instance running in the Oracle Cloud, complete with automatic wallet download and <a
     * href="https://docs.oracle.com/en-us/iaas/Content/secret-management/home.htm">Secret Management Service</a>
     * integration.
     *
     * @exception SQLException if a database error occurs
     */
    @Test
    void testConnectivity() throws SQLException {
        Config config = Services.get(Config.class);

        // For user experience and example simplicity, this unit test uses JUnit "assumptions" to make sure this test is
        // simply skipped if required configuration is not present (rather than failing). See
        // ../../../../../../../../../README.md for more about the required configuration. See
        // https://docs.junit.org/5.12.2/api/org.junit.jupiter.api/org/junit/jupiter/api/Assumptions.html for more about
        // JUnit assumptions.

        // Make sure the compartment.ocid property was set, usually as a System property.
        String compartmentOcid = config.get("compartment.ocid").asString().orElse(null);
        assumeTrue(compartmentOcid != null && !compartmentOcid.isBlank());

        // Make sure the database.ocid property was set, usually as a System property.
        String databaseOcid = config.get("database.ocid").asString().orElse(null);
        assumeTrue(databaseOcid != null && !databaseOcid.isBlank());

        // Make sure the vault.ocid property was set, usually as a System property.
        String vaultOcid = config.get("vault.ocid").asString().orElse(null);
        assumeTrue(vaultOcid != null && !vaultOcid.isBlank());

        // If the assumptions above passed, continue to run the test. Otherwise execution will effectively stop here.

        // Create a new AdbsDataSource that supplies physical connections to an Oracle Autonomous AI Database Serverless
        // instance running in the Oracle Cloud.
        AdbsDataSource ds = new AdbsDataSource();

        // Set its URL to the database OCID. If the URL is an OCID, then the special features of AdbsDataSource are
        // enabled. If it is not an OCID, then they are not, and the AdbsDataSource behaves just like the ordinary
        // oracle.jdbc.datasource.impl.OracleDataSource it extends
        // (https://docs.oracle.com/en/database/oracle/oracle-database/26/jajdb/oracle/jdbc/datasource/impl/OracleDataSource.html).
        //
        // See
        // ../../../../../../../../main/java/io/helidon/examples/integrations/oci/adbs/AdbsDataSource.java.
        ds.setURL(databaseOcid);

        // Ask Helidon Config for the value of the adbs.example.database.user and adbs.example.database.password
        // properties. Because Helidon's support for the Oracle Cloud's Secrets Management API is referenced in this
        // example, the Secrets Management API ("the vault") will be consulted for these properties. See
        // ../../../../../../../../main/resources/application.yaml and
        // ../../../../../../../../main/resources/meta-config.yaml.
        ds.setUser(config.get("adbs.example.database.user").asString().orElseThrow(AssertionError::new));
        ds.setPassword(config.get("adbs.example.database.password").asString().orElseThrow(AssertionError::new));

        // Connect to the database and prove the AdbsDataSource works properly.
        try (var c = ds.getConnection();
             var s = c.createStatement();
             var rs = s.executeQuery("SELECT 'Hello, world!';")) {
            assertThat(rs.next(), is(true));
            assertThat(rs.getString(1), is("Hello, world!"));
            assertThat(rs.next(), is(false));
        }

        // (Once the AdbsDataSource successfully connects, it will store the TNS entry name it found and used.)
        assertThat(ds.getTNSEntryName(), not(nullValue()));

    }

}
