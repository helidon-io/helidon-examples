/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.webserver.ratelimit;

import java.util.concurrent.Semaphore;

import io.helidon.config.Config;
import io.helidon.faulttolerance.Bulkhead;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

class SleepService  implements HttpService {

    private static final System.Logger LOGGER = System.getLogger(SleepService.class.getName());

    private static Semaphore rateLimitSem;
    private static Bulkhead rateLimitBulkhead;

    SleepService() {
        this(Config.global().get("app"));
    }

    private SleepService(Config appConfig) {
        int rateLimit = appConfig.get("ratelimit").asInt().orElse(20);
        LOGGER.log(System.Logger.Level.INFO, "         Application rate limit is " + rateLimit);

        /* Demonstrate rate limiting using two different mechanisms: Semaphore and FT bulkhead */
        rateLimitSem = new Semaphore(rateLimit);
        rateLimitBulkhead = Bulkhead.builder()
                .limit(rateLimit)
                .queueLength(rateLimit)
                .build();
    }

    @Override
    public void routing(HttpRules rules) {
        rules
                .get("/sleep/{seconds}", this::sleepHandler)
                .get("/sleep-bulkhead/{seconds}", this::sleepHandlerBulkhead);
    }

    /**
     * Sleep for a specified number of seconds.
     * The optional path parameter controls the number of seconds to sleep. Defaults to 1
     * Rate limited using Semaphore
     *
     * @param request  server request
     * @param response server response
     */
    private void sleepHandler(ServerRequest request, ServerResponse response) {
        int seconds = request.path().pathParameters().first("seconds").asInt().orElse(1);

        try {
            rateLimitSem.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            response.send(String.valueOf(sleep(seconds)));
        } finally {
            rateLimitSem.release();
        }
    }

    /**
     * Sleep for a specified number of seconds.
     * The optional path parameter controls the number of seconds to sleep. Defaults to 1
     * Rate limited using Bulkhead
     *
     * @param request  server request
     * @param response server response
     */
    private void sleepHandlerBulkhead(ServerRequest request, ServerResponse response) {
        int seconds = request.path().pathParameters().first("seconds").asInt().orElse(1);

        int result = rateLimitBulkhead.invoke(() -> sleep(seconds));
        response.send(String.valueOf(result));
    }

    /**
     * Sleep current thread.
     *
     * @param seconds number of seconds to sleep
     * @return number of seconds requested to sleep
     */
    private static int sleep(int seconds) {
        LOGGER.log(System.Logger.Level.DEBUG, Thread.currentThread() + ": Sleeping for " + seconds + " seconds");
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException e) {
            LOGGER.log(System.Logger.Level.WARNING, e);
        }
        return seconds;
    }
}
