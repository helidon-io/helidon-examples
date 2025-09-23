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
package io.helidon.examples.dbclient.oracle.ucp;

import java.sql.Connection;
import java.sql.SQLException;

import io.helidon.config.Config;
import io.helidon.dbclient.jdbc.JdbcConnectionPool;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

class UcpConnectionPool implements JdbcConnectionPool {

    private final PoolDataSource pds;

    UcpConnectionPool(Config config) {
        try {
            pds = PoolDataSourceFactory.getPoolDataSource();
            pds.setURL(config.get("url").asString().get());
            pds.setUser(config.get("username").asString().get());
            pds.setPassword(config.get("password").asString().get());
            pds.setConnectionPoolName(config.get("poolName").asString().get());
            pds.setConnectionFactoryClassName(config.get("factoryClassName").asString().get());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection connection() {
        try {
            return pds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
