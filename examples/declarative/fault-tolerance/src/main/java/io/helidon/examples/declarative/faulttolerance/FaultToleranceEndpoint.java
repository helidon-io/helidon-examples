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

package io.helidon.examples.declarative.faulttolerance;

import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.faulttolerance.Ft;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings("deprecation")
@RestServer.Endpoint // webserver declarative endpoint
@Http.Path("/ft") // path to serve this endpoint on
@Service.Singleton // service registry scope (must be singleton)
class FaultToleranceEndpoint {
    private final AtomicInteger retryCount = new AtomicInteger();

    FaultToleranceEndpoint() {
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/fallback")
    @Ft.Fallback(value = "helloFallback", applyOn = IllegalStateException.class)
    String hello() {
        failingOperation();
        return "Should have failed";
    }

    String helloFallback() {
        return "Fallback";
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/retry")
    @Ft.Retry(calls = 2)
    String retry() {
        int count = retryCount.incrementAndGet();
        if (count % 2 == 0) {
            return "Success";
        }
        System.out.println("Throwing");
        throw new IllegalStateException("Should retry");
    }

    private void failingOperation() {
        throw new IllegalStateException("Failed");
    }
}
