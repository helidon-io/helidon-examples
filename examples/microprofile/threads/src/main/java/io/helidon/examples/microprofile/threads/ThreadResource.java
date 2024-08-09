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

package io.helidon.examples.microprofile.threads;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.helidon.common.configurable.ThreadPoolSupplier;
import io.helidon.microprofile.cdi.ExecuteOn;
import io.helidon.microprofile.server.ServerCdiExtension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

/**
 * Resource class for demonstrating threading.
 */
@Path("/thread")
@ApplicationScoped
public class ThreadResource {

    private static final System.Logger LOGGER = System.getLogger(ThreadResource.class.getName());
    private static final Random RAND = new Random(System.currentTimeMillis());
    private static final Client CLIENT = ClientBuilder.newClient();

    @Inject
    private ServerCdiExtension serverExtension;

    // Executor of virtual threads.
    private final ExecutorService virtualExecutorService = ThreadPoolSupplier.builder()
            .threadNamePrefix("application-virtual-executor-")
            .virtualThreads(true)
            .build()
            .get();
    /**
     * Performs a CPU intensive operation. Uses the @ExecuteOn annotation
     * to have this handler executed on a platform thread (instead of a virtual
     * thread which is the default for Helidon 4).
     * @param iterations number of compute iterations to perform
     * @return Result of computation
     */
    @Path("/compute/{iterations}")
    @GET
    @ExecuteOn(ExecuteOn.ThreadType.PLATFORM)
    public String computeHandler(@PathParam("iterations") int iterations) {
        if (iterations < 1) {
            iterations = 1;
        }
        return Double.toString(compute(iterations));
    }

    /**
     * Perform a fanout operation to simulate concurrent calls to remove services.
     *
     * @param count number of remote calls to make
     * @return aggregated values returned by remote call
     */
    @Path("/fanout/{count}")
    @GET
    public Response fanoutHandler(@PathParam("count") int count) {
        if (count < 1) {
            count = 1;
        }

        // We simulate multiple client requests running in parallel by calling our sleep endpoint.
        try {
            // For this we use our virtual thread based executor. We submit the work and save the Futures
            var futures = new ArrayList<Future<String>>();
            for (int i = 0; i < count; i++) {
                futures.add(virtualExecutorService.submit(() -> callRemote(RAND.nextInt(5))));
            }

            // After work has been submitted we loop through the future and block getting the results.
            // We aggregate the results in a list of Strings
            var responses = new ArrayList<String>();
            for (var future : futures) {
                try {
                    responses.add(future.get());
                } catch (InterruptedException e) {
                    responses.add(e.getMessage());
                }
            }

            // All parallel calls are complete!
            return Response.ok().entity(String.join(":", responses)).build();
        } catch (ExecutionException e) {
            LOGGER.log(System.Logger.Level.ERROR, e);
            return Response.status(500).build();
        }
    }

    /**
     * Sleep for a specified number of seconds.
     * The optional path parameter controls the number of seconds to sleep. Defaults to 1
     *
     * @param seconds number of seconds to sleep
     * @return number of seconds requested to sleep
     */
    @Path("/sleep/{seconds}")
    @GET
    public String sleepHandler(@PathParam("seconds") int seconds) {
        if (seconds < 1) {
            seconds = 1;
        }
        sleep(seconds);
        return String.valueOf(seconds);
    }

    /**
     * Perform a CPU intensive computation.
     *
     * @param iterations: number of times to perform computation
     * @return result of computation
     */
    private double compute(int iterations) {
        LOGGER.log(System.Logger.Level.INFO, Thread.currentThread() + ": Computing with " + iterations + " iterations");
        double d = 123456789.123456789 * RAND.nextInt(100);
        for (int i = 0; i < iterations; i++) {
            for (int n = 0; n < 1_000_000; n++) {
                for (int j = 0; j < 5; j++) {
                    d = Math.tan(d);
                    d = Math.atan(d);
                }
            }
        }
        return d;
    }

    /**
     * Sleep current thread.
     *
     * @param seconds number of seconds to sleep
     * @return number of seconds requested to sleep
     */
    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException e) {
            LOGGER.log(System.Logger.Level.WARNING, e);
        }
    }

    /**
     * Simulate a remote client call by calling this server's sleep endpoint.
     *
     * @param seconds number of seconds the endpoint should sleep.
     * @return string response from client
     */
    private String callRemote(int seconds) {
        LOGGER.log(System.Logger.Level.INFO, Thread.currentThread() + ": Calling remote sleep for " + seconds + "s");
        Response response = CLIENT.target("http://localhost:" + serverExtension.port() + "/thread/sleep/" + seconds)
                .request()
                .get();

        String msg;
        if (response.getStatus() == 200) {
            msg = response.readEntity(String.class);
        }  else {
            msg = Integer.toString(response.getStatus());
        }
        response.close();
        return msg;
    }
}
