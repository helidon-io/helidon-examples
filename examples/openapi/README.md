
# Helidon SE OpenAPI Example

This example shows a simple greeting application, similar to the one from the 
Helidon SE QuickStart, enhanced with OpenAPI support.

The OpenAPI document in this example comes from a static file packaged
with the application.

## Build and run

```shell
mvn package
java -jar target/helidon-examples-openapi.jar
```

Try the endpoints:

```shell
curl -X GET http://localhost:8080/greet
#Output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
#Output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
#Output: {"message":"Hola Jose!"}

curl -X GET http://localhost:8080/openapi
#Output: [lengthy OpenAPI document]
```

The output describes the endpoints in `GreetService` as described in
the static file.

## Customizing OpenAPI Behavior
Helidon automatically discovers that OpenAPI is on the classpath, initializes it from configuration, and 
adds OpenAPI support to the webserver.

You can customize the behavior of OpenAPI in two ways.

### Customizing via Configuration
The `application.yaml` in the example project already contains a section for `server`. 
To customize the OpenAPI behavior, add a `features` subsection to `server` as shown below.
```yaml
server:
  port: 8080
  host: 0.0.0.0
  features:
    openapi:
      web-context: myopenapi
```
The configuration changes the endpoint that serves the OpenAPI document from
the default `/openapi` to `/myopenapi`. 

The [documentation for OpenAPI configuration](https://helidon.io/docs/v4/config/io_helidon_openapi_OpenApiFeature) shows all the settings available for customization.

### Customization via Code
As written, the `Main#setup` method in this example project initializes the Helidon webserver using the following code:
```java
server.config(config.get("server"))
       .routing(Main::routing);
```
This code allows Helidon to automatically find any webserver features on the classpath and
add them to the webserver, using any relevant configuration to prepare each feature.
This includes the OpenAPI feature.

However, you can explicitly prepare a feature and add it to the webserver.
The following change customizes the endpoint that serves the OpenAPI document.
```java
server.config(config.get("server"))
        .addFeature(OpenApiFeature.builder()
                .webContext("myopenapi")
                .config(config.get("server.features.openapi"))
                .build())
        .routing(Main::routing);
```
Note that this revision also applies any configuration for the OpenAPI feature and does so _after_ 
setting the web context endpoint. In this way the developer can change the default endpoint while still
allowing end users to customize the actual endpoint Helidon uses.

See the [Javadoc for `OpenApiFeatureConfig.Builder`](https://helidon.io/docs/v4/apidocs/io.helidon.openapi/io/helidon/openapi/OpenApiFeatureConfig.Builder.html)
for more information.
