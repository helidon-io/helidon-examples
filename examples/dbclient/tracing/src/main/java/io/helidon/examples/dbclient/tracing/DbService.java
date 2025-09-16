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
package io.helidon.examples.dbclient.tracing;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.http.NotFoundException;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

class DbService implements HttpService {

    private static final System.Logger LOGGER = System.getLogger(DbService.class.getName());

    private final DbClient db;

    DbService(Config config) {
        db = DbClient.create(config);
        if (config.get("init").asBoolean().orElse(true)) {
            init();
        }
    }

    private void init() {
        long count = db.execute()
                .createDmlStatement("CREATE TABLE store (k VARCHAR(64) NOT NULL PRIMARY key, v VARCHAR(32))")
                .execute();
        LOGGER.log(System.Logger.Level.INFO, "Executed {0} statements", count);
    }

    @Override
    public void routing(HttpRules rules) {
        rules
                .post("/{key}", this::create)
                .get("/{key}", this::get)
                .put("/{key}", this::update)
                .delete("/{key}", this::delete);
    }

    private void create(ServerRequest req, ServerResponse res) {
        String key = req.path().pathParameters().first("key").orElse(null);
        String value = req.content().asOptional(String.class).orElse(null);
        if (key == null || value == null) {
            res.status(400);
            res.send();
        } else {
            long count = db.execute().createInsert("INSERT INTO store VALUES(:k, :v)")
                    .addParam("k", key)
                    .addParam("v", value)
                    .execute();
            res.status(201);
            res.send("Inserted: " + count + " values");
        }
    }

    private void get(ServerRequest req, ServerResponse res) {
        String key = req.path().pathParameters().get("key");
        res.send(db.execute()
                .createGet("SELECT * FROM store WHERE k = ?")
                .addParam(key)
                .execute()
                .orElseThrow(() -> new NotFoundException("key not found: " + key))
                .column("v").getString());
    }

    private void update(ServerRequest req, ServerResponse res) {
        String key = req.path().pathParameters().first("key").orElse(null);
        String value = req.content().asOptional(String.class).orElse(null);
        if (key == null || value == null) {
            res.status(400);
            res.send();
        } else {
            long count = db.execute()
                    .createUpdate("UPDATE store SET v = :v WHERE k = :k")
                    .addParam("k", key)
                    .addParam("v", value)
                    .execute();
            res.send("Updated: " + count + " values");
        }
    }

    private void delete(ServerRequest req, ServerResponse res) {
        String key = req.path().pathParameters().get("key");
        long count = db.execute()
                .createDelete("DELETE FROM store WHERE k = ?")
                .addParam(key)
                .execute();
        res.send("Deleted: " + count + " values");
    }

}
