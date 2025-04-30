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
package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai;

import java.util.function.Supplier;

import java.sql.SQLException;
import javax.sql.DataSource;

import io.helidon.common.config.Config;
import io.helidon.service.registry.Service;

import oracle.jdbc.pool.OracleDataSource;
/**
 * A service factory that provides an instance of {@link javax.sql.DataSource}.
 *
 * This class implements {@link Supplier} to supply a default DataSource.
 */
@Service.Singleton
public class DataSourceFactory implements Supplier<DataSource>{

    @Service.Inject
    Config config;

    /**
     * Data source for Oracle database 23ai.
     */
    @Override
    public DataSource get() {
        OracleDataSource dataSource = null;
        try {
            dataSource = new OracleDataSource();
            dataSource.setURL(config.get("db.url").as(String.class)
                    .orElseThrow(() -> new IllegalStateException("Missing URL!!")));
            dataSource.setUser(config.get("db.userName").as(String.class).orElse("ADMIN"));
            dataSource.setPassword(config.get("db.password").as(String.class)
                                           .orElseThrow(() -> new IllegalStateException("Missing password!!")));
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
        return dataSource;
    }
}
