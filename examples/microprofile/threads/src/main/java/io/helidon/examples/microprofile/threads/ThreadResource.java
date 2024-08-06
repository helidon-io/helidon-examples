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

@Path("/thread")
@ApplicationScoped
public class ThreadResource {

    private static final System.Logger LOGGER = System.getLogger(ThreadResource.class.getName());
    private static final Random rand = new Random(System.currentTimeMillis());
    private static final Client client = ClientBuilder.newClient();

    @Inject
    ServerCdiExtension serverExtension;

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
     * @param iterations
     * @return
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
                futures.add(virtualExecutorService.submit(() -> callRemote(rand.nextInt(5))));
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
     */
    @Path("/sleep/{seconds}")
    @GET
    public String sleepHandler(@PathParam("seconds") int seconds) {
        if (seconds < 1) {
            seconds = 1;
        }
        return String.valueOf(sleep(seconds));
    }

    /**
     * Perform a CPU intensive computation
     * We use the @ExecuteOne annotation to inform Helidon to run this
     * CPU intensive operation on a platform thread to avoid the virtual
     * thread.
     *
     * @param iterations: number of times to perform computation
     * @return result of computation
     */
    private double compute(int iterations) {
        LOGGER.log(System.Logger.Level.INFO, Thread.currentThread() + ": Computing with " + iterations + " iterations");
        double d = 123456789.123456789 * rand.nextInt(100);
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
     * Sleep current thread
     *
     * @param seconds number of seconds to sleep
     * @return number of seconds requested to sleep
     */
    private int sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException e) {
            LOGGER.log(System.Logger.Level.WARNING, e);
        }
        return seconds;
    }

    /**
     * Simulate a remote client call by calling this server's sleep endpoint
     *
     * @param seconds number of seconds the endpoint should sleep.
     * @return string response from client
     */
    private String callRemote(int seconds) {
        LOGGER.log(System.Logger.Level.INFO, Thread.currentThread() + ": Calling remote sleep for " + seconds + "s");
        Response response = client.target("http://localhost:" + serverExtension.port() + "/thread/sleep/" + seconds)
                .request()
                .get();
        if (response.getStatus() == 200) {
            return response.readEntity(String.class);
        }
        return Integer.toString(response.getStatus());
    }
}