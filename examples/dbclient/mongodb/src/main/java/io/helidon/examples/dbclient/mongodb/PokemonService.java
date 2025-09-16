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

package io.helidon.examples.dbclient.mongodb;

import java.util.List;
import java.util.Map;

import io.helidon.common.parameters.Parameters;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

/**
 * A CRUD Http service backed by a database.
 */
class PokemonService implements HttpService {

    private static final JsonBuilderFactory JSON_BUILDER_FACTORY = Json.createBuilderFactory(Map.of());

    private final DbClient db;

    PokemonService(Config config) {
        db = DbClient.create(config);
    }

    @Override
    public void routing(HttpRules rules) {
        rules
                .post("/", this::create)
                .get("/", this::list)
                .get("/{name}", this::get)
                .put("/{name}", this::update)
                .delete("/", this::deleteAll)
                .delete("/{name}", this::delete);
    }

    private void list(ServerRequest req, ServerResponse res) {
        List<DbRow> rows = db.execute()
                .createQuery("""
                        {
                            "collection": "pokemons",
                            "query": {}
                        }
                        """)
                .execute()
                .toList();
        JsonArrayBuilder array = JSON_BUILDER_FACTORY.createArrayBuilder();
        for (DbRow row : rows) {
            array.add(toJson(row));
        }
        res.send(array.build());
    }

    private void create(ServerRequest req, ServerResponse res) {
        JsonObject json = req.content().as(JsonObject.class);
        String name = json.getString("name", null);
        String type = json.getString("type", null);
        if (name == null || type == null) {
            res.status(400);
            res.send();
        } else {
            long count = db.execute()
                    .createInsert("""
                            {
                                "collection": "pokemons",
                                "value": {
                                    "_id": $name,
                                    "type": $type
                                }
                            }
                            """)
                    .addParam("name", name)
                    .addParam("type", type)
                    .execute();
            res.status(201);
            res.send("Inserted: " + count + " values");
        }
    }

    private void get(ServerRequest req, ServerResponse res) {
        String name = req.path().pathParameters().get("name");
        DbRow row = db.execute()
                .createGet("""
                        {
                            "collection": "pokemons",
                            "query": {
                              "_id": ?
                            }
                        }
                        """)
                .addParam(name)
                .execute()
                .orElse(null);
        if (row == null) {
            res.status(404);
            res.send("Pokemon " + name + " not found");
        } else {
            res.send(toJson(row));
        }
    }

    private void update(ServerRequest req, ServerResponse res) {
        Parameters params = req.path().pathParameters();
        String name = params.get("name");
        JsonObject json = req.content().as(JsonObject.class);
        String type = json.getString("type", null);
        if (name == null || type == null) {
            res.status(400);
            res.send();
        } else {
            long count = db.execute()
                    .createUpdate("""
                            {
                                "collection": "pokemons",
                                "query": {
                                    "_id": $name
                                },
                                "value": {
                                    $set: { "type": $type }
                                }
                            }
                            """)
                    .addParam("name", name)
                    .addParam("type", type)
                    .execute();
            res.send("Updated: " + count + " values");
        }
    }

    private void delete(ServerRequest req, ServerResponse res) {
        String name = req.path().pathParameters().get("name");
        long count = db.execute()
                .createDelete("""
                        {
                            "collection": "pokemons",
                            "query": {
                                "_id": ?
                            }
                        }
                        """)
                .addParam(name)
                .execute();
        res.send("Deleted: " + count + " values");
    }

    private void deleteAll(ServerRequest req, ServerResponse res) {
        long count = db.execute()
                .createDelete("""
                        {
                            "collection": "pokemons",
                            "operation": "delete"
                        }
                        """)
                .execute();
        res.send("Deleted: " + count + " values");
    }

    private static JsonObject toJson(DbRow row) {
        return JSON_BUILDER_FACTORY.createObjectBuilder()
                .add("name", row.column("_id").getString())
                .add("type", row.column("type").getString())
                .build();
    }
}
