package io.helidon.examples.quickstart.inject;

import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;

/**
 * Main class to start the application using service registry.
 */
// generate "stub" binding during compilation, so we can use it from this class
// the Maven plugin then overwrites it with proper information
// this should only be done in the Main class, as it needs to gather information from all dependencies of the application
@Service.GenerateBinding
public class Main {
    static {
        // initialize logging at build time (for native-image build)
        LogConfig.initClass();
    }

    /**
     * Start the application.
     *
     * @param args command line arguments, currently ignored
     */
    public static void main(String[] args) {
        // initialize logging at runtime
        // (if in GraalVM native image, this will re-configure logging with runtime configuration)
        LogConfig.configureRuntime();

        // start the service registry - uses generated application binding to avoid reflection and runtime lookup
        ServiceRegistryManager.start(ApplicationBinding.create());
    }
}
