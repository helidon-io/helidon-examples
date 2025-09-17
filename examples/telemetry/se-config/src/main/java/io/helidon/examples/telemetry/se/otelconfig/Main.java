
package io.helidon.examples.telemetry.se.otelconfig;


import io.helidon.logging.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

import io.opentelemetry.api.OpenTelemetry;

/**
 * The application main class.
 */
public class Main {


    /**
     * Cannot be instantiated.
     */
    private Main() {
    }


    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        
        // load logging configuration
        LogConfig.configureRuntime();

        // initialize config from default configuration
        Config config = Services.get(Config.class);

        // Fetch the OpenTelemetry instance from the service registry to make sure it is initialized
        // and assigned as the global OpenTelemetry object early during start-up.
//        Services.get(OpenTelemetry.class);

        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(Main::routing)
                .build()
                .start();


        System.out.println("WEB server is up! http://localhost:" + server.port() + "/simple-greet");

    }


    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing
               .register("/greet", new GreetService())
               .get("/simple-greet", (req, res) -> res.send("Hello World!")); 
    }
}