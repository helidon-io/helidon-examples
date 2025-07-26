# Helidon MP Dynamic Configuration Example

This example implements a simple REST service using MicroProfile demonstrating dynamic-config.

## Usage

To be able to use dynamic configuration add the following dependency: 

```xml
<dependency>
    <groupId>io.helidon.microprofile.config</groupId>
    <artifactId>helidon-microprofile-config</artifactId>
</dependency>
```

## Build and run

### Prerequisite
 - Java 21
 - Maven 3.6.*

---

### Example-1 Reading from `META-INF/microprofile-config.properties` file
```shell
mvn clean package
java -jar target/helidon-examples-microprofile-dynamic-config.jar
```

#### Exercise the Example-1 application

```shell
curl --silent -X GET http://localhost:8080/dynamic/config | jq .
```
Response:
> {
>   "startupMessage": "MP Message from application config props.",
>   "appLogLevel": "WARN"
> }

---

### Example-2 Overriding with java vm arguments
```shell
mvn package
java -jar -Dapp.startup.message='Override Message From Terminal' target/helidon-examples-microprofile-dynamic-config.jar
```
#### Exercise the Example-2 application

```shell
curl --silent -X GET http://localhost:8080/dynamic/config | jq .
```
Response:
>
> {
>    "startupMessage": "Override Message From Terminal"
>    "appLogLevel": "WARN"
> ｝

---

### Example-3 Setting with env vars
* Convention is to convert the configuration key to uppercase and replace dots (.) with underscores (_).

      | Configuration key    | Env Var             | 
      |----------------------|---------------------|
      | app.log.level        | APP_LOG_LEVEL       |
      | app.startup.message  | APP_STARTUP_MESSAGE |

```shell
mvn package
export APP_LOG_LEVEL=TRACE
export APP_STARTUP_MESSAGE='Env Vars Startup Message'
java -jar target/helidon-examples-microprofile-dynamic-config.jar
```
#### Exercise the Example-3 application

```shell
curl --silent -X GET http://localhost:8080/dynamic/config | jq .
```
Response:
> {
>   "startupMessage": "Env Vars Startup Message" ,
>   "appLogLevel": "TRACE"
> }

---

### Example-4 Setting with URL Prop Config
For example enabled mock server at http://localhost:9192/api/mp/properties/dyanmic.configs,
which returns json response with multiple key value pairs.
```shell
curl --silent -X GET http://localhost:9192/api/mp/properties/dyanmic.configs
```
Response:
> {
>   'MP_URL_KEY1': 'MP_URL_PROPS_VALUE1',
>   'MP_URL_KEY2': 'MP_URL_PROPS_VALUE2'
> }

Start example with env var defined 
```shell
mvn package
export APP_LOG_LEVEL=TRACE
export APP_STARTUP_MESSAGE='Env Vars Startup Message'
java -jar target/helidon-examples-microprofile-dynamic-config.jar
```
#### Exercise the Example-4 application

```shell
 curl --silent -X GET http://localhost:8080/dynamic/config | jq . 
 ```
Response:
>
> {
>   "startupMessage": "MP Message from application config props.",
>   "appLogLevel": "WARN",
>   "appOptionalMessage": "Hello, Here's Application Optional Message",
>   "getDefaultMessage": "Hello, Here's Application Default Message",
>   "getUrlConfigSources": "{'MP_URL_KEY1'='MP_URL_PROPS_VALUE1', 'MP_URL_KEY2'='MP_URL_PROPS_VALUE2',}"
> }
>