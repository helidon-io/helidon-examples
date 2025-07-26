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

package io.helidon.examples.microprofile.dynamic.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.helidon.config.mp.MpConfigSources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.net.URL;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


/**
 * The DynamicProperties class provides access to application properties and configuration. It
 * injects properties from application configuration or Java VM command arguments.
 *
 * @author [Dasarathi Rout]
 */
@ApplicationScoped
public class DynamicProperties {

  /** The application startup message injected from configuration. */
  @Inject
  @ConfigProperty(name = "app.startup.message")
  private volatile String appStartupMessage;

  /** The configuration instance injected for accessing other properties. */
  @Inject private Config config;

  private String defaultMessage;

  /** Constructs a new DynamicProperties instance, initializing the current message. */
  @Inject
  public DynamicProperties(
      @ConfigProperty(
              name = "app.default.message",
              defaultValue = "Hello, Here's Application Default Message")
          String defaultMessage) {
    this.defaultMessage = defaultMessage;
    configSourceUrl();
  }

  /**
   * Returns the application startup message.
   *
   * @return the application startup message
   */
  public String getAppStartupMessage() {
    return this.appStartupMessage;
  }

  /**
   * Returns the application log level.
   *
   * @return the application log level as a string
   */
  public String getAppLogLevel() {
    return config.getValue("app.log.level", String.class);
  }

  public String getOptionalMessage() {
    return config
        .getOptionalValue("app.optional.message", String.class)
        .orElse("Hello, Here's Application Optional Message");
  }

  public String getDefaultMessage() {
    return this.defaultMessage;
  }

  public Map <String,String> getUrlConfigSources() throws Exception {
    ConfigSource urlConfigSource = MpConfigSources.create(new URL("http://localhost:9192/api/mp/properties/dyanmic.configs"));
    return urlConfigSource.getProperties();
  }

  /**
   * Setup for URL MpConfigSources.create(URL).
   * curl --silent -X GET http://localhost:9192/api/mp/properties/dyanmic.configs
   * {
   *   "MP_URL_KEY1": "MP_URL_PROPS_VALUE1",
   *   "MP_URL_KEY2'": "MP_URL_PROPS_VALUE2"
   * }
   * */
  private static void configSourceUrl() {
    int mockPort = 9192;
    WireMockServer wireMockServer = new WireMockServer(mockPort);
    wireMockServer.start();
    WireMock.configureFor("localhost", mockPort);

    stubFor(
            get(urlMatching("/api/mp/properties/.*"))
                    .willReturn(
                            aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(
                                            """
                                            {
                                              'MP_URL_KEY1': 'MP_URL_PROPS_VALUE1',
                                              'MP_URL_KEY2': 'MP_URL_PROPS_VALUE2'
                                            }
                                            """
                                    )));
  }
}
