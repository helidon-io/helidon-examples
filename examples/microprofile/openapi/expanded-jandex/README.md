# Helidon MP OpenAPI Expanded Jandex Indexing Example

This example shows a simple greeting application, similar to the basic OpenAPI MP example, but with proper handling of types from _outside_ this project that are used in resource method signatures.

### Why might I need expanded Jandex handling?
Many Helidon MP applications, including those created by the Helidon command line and the starter, run the Jandex plug-in to create an index file for the types defined in the project. 
Helidon's OpenAPI support leverages much of SmallRye's OpenAPI implementation and SmallRye scans the resource methods in the application to generate an OpenAPI model. This scan uses the Jandex index file directly to get information about the types used in resource methods, and SmallRye uses this information in preparing the OpenAPI document.

If a resource method's signature uses a type _outside_ the current project SmallRye's scan cannot usually find the type information because the Jandex index created by the typical project describes only the types defined in the current project, not types in any dependencies. 

### How does this affect my application?
In such cases, SmallRye logs warning messages about the types it cannot find. This can clutter the application output.

More importantly, the resulting OpenAPI model is less robust. When SmallRye cannot find a type it has no choice but to model that type as an opaque `Object`. The resulting OpenAPI model and document are less useful than they could be. 

Note that if the dependency contains _its own_ Jandex index for its own types then SmallRye can find information about those types.

### What does this example do?
This example shows how to expand the Jandex index built by your project to include selected types from _outside_ the project.
There are two key differences from the basic OpenAPI example:

1. The new resource method `mediaType`added to `GreetingResource` returns a `MediaType` from Jarkarta RESTful Web Wervices, a type from outside this project.
2. The `pom.xml` configures its invocation of the Jandex plug-in to include that type in the generated Jandex index.

The example `pom.xml` adds a single type from a single dependency to the index. If you need to for your application add more dependencies and more include entries. 

## Build and run

```shell
mvn package
java -jar target/helidon-examples-microprofile-openapi-expanded-jandex.jar
```

Try the endpoints. These are the same actions supported by the basic OpenAPI example:

```shell
curl -X GET http://localhost:8080/greet
#Output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
#Output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"message" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
#Output: {"message":"Hola Jose!"}

curl -X GET http://localhost:8080/openapi
#Output: [lengthy OpenAPI document]
```
After running the last command notice that the OpenAPI document's `components/schemas` section contains a declaration for `MediaType` that includes the `MediaType` properties (`type`, `subtype`, etc.). Further, the document's entry for `paths/greet/mediatype:get` declares the response content by referring to that exact `#/components/schemas/MediaType` entry.

## Building and running without expanded Jandex processing
You can simulate the build without the expanded Jandex processing to see the warning SmallRye logs during startup and to see the difference in the generated OpenAPI document.
When you build the project specify the `normal-jandex` Maven profile and skip the unit tests.

```shell
mvn clean package -Pnormal-jandex -DskipTests
java -jar target/helidon-examples-microprofile-openapi-expanded-jandex.jar
```
As the app starts notice a message like the following in the app output:
```list
WARN io.smallrye.openapi.runtime.scanner Thread[#1,main,5,main]: SROAP04005: Could not find schema class in index: jakarta.ws.rs.core.MediaType
```

Retrieve the OpenAPI document:
```shell
curl -X GET http://localhost:8080/openapi
```
Notice two things about the output:
1. The `components/schemas` section contains no entry for `MediaType`. That's expected given that SmallRye could not find the type information for it.
2. The response content for `paths/greet/mediatype:get` is simply `object`.
   
   While it is true that the response is an `Object` this version of the OpenAPI document for your app is less useful than the earlier one because of the missing type information for `MediaType`.