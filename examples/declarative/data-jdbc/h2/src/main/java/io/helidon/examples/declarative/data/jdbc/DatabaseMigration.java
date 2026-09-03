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
package io.helidon.examples.declarative.data.jdbc;

import javax.sql.DataSource;

import io.helidon.service.registry.Service;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

/**
 * Migrates the example database before the web server starts.
 */
@Service.Singleton
@Service.RunLevel(Service.RunLevel.STARTUP)
final class DatabaseMigration {

    private static final System.Logger LOGGER = System.getLogger(DatabaseMigration.class.getName());
    private static final String MIGRATION_LOCATION = "classpath:db/migration";

    private final DataSource dataSource;

    /**
     * Creates the database migration service with the example data source.
     *
     * @param dataSource registry managed data source
     */
    @Service.Inject
    DatabaseMigration(@Service.Named("example") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Applies pending database migrations.
     */
    @Service.PostConstruct
    void migrate() {
        migrate(dataSource);
    }

    /**
     * Applies pending database migrations to the provided data source.
     *
     * @param dataSource target data source
     * @return migration result
     */
    static MigrateResult migrate(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(MIGRATION_LOCATION)
                .validateOnMigrate(true)
                .baselineOnMigrate(false)
                .outOfOrder(false)
                .cleanDisabled(true)
                .load();
        MigrateResult result = flyway.migrate();
        String schemaVersion = result.targetSchemaVersion == null
                ? result.initialSchemaVersion
                : result.targetSchemaVersion;
        LOGGER.log(System.Logger.Level.INFO,
                   "Database schema is at version {0}; applied {1} migration(s)",
                   schemaVersion,
                   result.migrationsExecuted);
        return result;
    }
}
