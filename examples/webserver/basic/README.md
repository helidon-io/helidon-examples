##WebServer Basic Example

This example demonstrates a minimal Helidon WebServer application that starts an HTTP server and exposes simple routes.

##Project Structure

```
webserver-basic/
 ├── src
 │   └── main
 │       └── java
 │           └── io/helidon/examples/webserver/basic/
 │               ├── Main.java
 │               └── GreetService.java
 ├── pom.xml
 └── README.md

```
##Build

Use Maven to compile the project:

mvn clean install

Run

Start the WebServer using:

java -jar target/webserver-basic.jar


The server will start on the default port (8080) unless configured otherwise.

Exercise the Application

After the application is running, you can call the available endpoints:

Root endpoint
curl -X GET http://localhost:8080

Greet endpoint
curl -X GET http://localhost:8080/greet

Personalized greeting
curl -X GET http://localhost:8080/greet/{name}


This will return:
{"message": "Hello {name}!"}

Update the greeting
curl -X PUT -H "Content-Type: application/json" \
    -d '{"greeting": "Hola"}' \
    http://localhost:8080/greet/greeting

##Notes

This example follows the standard Helidon project layout.

For more advanced server features, refer to other examples in the repository.