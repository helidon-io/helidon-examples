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
package io.helidon.examples.integrations.cdi.datasource.hikaricp.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * A simple resource that uses JDBC.
 */
@Path("/tables")
@ApplicationScoped
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public class TablesResource {

    private final DataSource ds;

    @Inject
    public TablesResource(@Named("ds1") DataSource ds) {
        this.ds = Objects.requireNonNull(ds);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = this.ds.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT TABLE_NAME
                        FROM INFORMATION_SCHEMA.TABLES
                        ORDER BY TABLE_NAME ASC
                    """);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString(1)).append("\n");
            }
        }
        return sb.toString();
    }
}
