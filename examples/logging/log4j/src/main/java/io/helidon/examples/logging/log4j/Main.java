/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.logging.log4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.logging.common.HelidonMdc;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;

/**
 * Main class of the example, runnable from command line.
 * There is a limitation of log4j in native image - we only have loggers that are
 * initialized after we configure logging, which unfortunately excludes Helidon loggers.
 * You would need to use JUL or slf4j to have Helidon logs combined with application logs.
 */
public final class Main {
    private static System.Logger systemLogger;
    private static Logger logger;

    private Main() {
    }

    /**
     * Starts the example.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        // file based logging configuration does not work
        // with native image!
        configureLog4j();
        LogConfig.configureRuntime();
        // get logger after configuration
        logger = LogManager.getLogger(Main.class);
        systemLogger = System.getLogger(Main.class.getName());

        // the Helidon context is used to propagate MDC across threads
        // if running within Helidon WebServer, you do not need to runInContext, as that is already
        // done by the webserver
        Contexts.runInContext(Context.create(), Main::logging);

        WebServer server = WebServer.builder()
                .routing(Main::routing)
                .build()
                .start();
    }

    private static void routing(HttpRouting.Builder routing) {
        routing.get("/", (req, res) -> {
            HelidonMdc.set("name", String.valueOf(req.id()));
            logger.info("Running in webserver, id:");
            res.send("Hello");
        });
    }

    private static void logging() {
        HelidonMdc.set("name", "startup");
        logger.info("Starting up");
        systemLogger.log(System.Logger.Level.INFO, "Using System logger");

        // now let's see propagation across executor service boundary, we can also use Log4j's ThreadContext
        ThreadContext.put("name", "propagated");
        // wrap executor so it supports Helidon context, this is done for all built-in executors in Helidon
        ExecutorService es = Contexts.wrap(Executors.newSingleThreadExecutor());

        Future<?> submit = es.submit(Main::log);
        try {
            submit.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();
    }

    private static void log() {
        logger.info("Running on another thread");
    }

    private static void configureLog4j() {
        // configure log4j
        final var builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName("root");
        builder.setStatusLevel(Level.INFO);
        final var appenderComponentBuilder = builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderComponentBuilder.add(builder.newLayout("PatternLayout")
                                             .addAttribute("pattern", "%d{HH:mm:ss.SSS} %-5level [%t] %logger{36} - %msg "
                                                     + "\"%X{name}\"%n"));
        builder.add(appenderComponentBuilder);
        builder.add(builder.newRootLogger(Level.INFO)
                            .add(builder.newAppenderRef("Stdout")));
        Configurator.initialize(builder.build());
    }
}
