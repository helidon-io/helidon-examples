/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.webserver.echo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import io.helidon.common.parameters.Parameters;
import io.helidon.common.uri.UriQuery;
import io.helidon.http.Header;
import io.helidon.http.Headers;
import io.helidon.http.RoutedPath;
import io.helidon.http.Status;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Echo example.
 */
public class EchoMain {
    private EchoMain() {
    }

    /**
     * Main method.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        LogConfig.configureRuntime();

        WebServer.builder()
                .port(8080)
                .host("127.0.0.1")
                .routing(router -> router
                        .get("/echo/{param}", EchoMain::echo)
                )
                .build()
                .start();
    }

    private static void echo(ServerRequest req, ServerResponse res) {
        RoutedPath path = req.path();
        UriQuery query = req.query();
        Headers headers = req.headers();

        Parameters pathParams = path.matrixParameters();
        Parameters templateParams = path.pathParameters();
        Set<String> queryNames = query.names();

        for (String pathParamName : pathParams.names()) {
            res.header("R-PATH_PARAM_" + pathParamName, pathParams.get(pathParamName));
        }

        for (String paramName : templateParams.names()) {
            res.header("R-PATH_" + paramName, templateParams.get(paramName));
        }

        for (String queryName : queryNames) {
            res.header("R-QUERY_" + queryName, query.all(queryName).toString());
        }

        for (Header header : headers) {
            res.header("R-" + header.name(), header.allValues().toString());
        }

        try (InputStream inputStream = req.content().inputStream();
                OutputStream outputStream = res.outputStream()) {
            inputStream.transferTo(outputStream);
        } catch (Exception e) {
            res.status(Status.INTERNAL_SERVER_ERROR_500).send("failed: " + e.getMessage());
        }
    }
}
