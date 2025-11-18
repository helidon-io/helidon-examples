🚀 Helidon WebServer – Basic Example

This example demonstrates the simplest possible Helidon SE WebServer application.
It shows how to start a server, define basic routing, handle HTTP requests, and return responses.

This is a good starting point for understanding how Helidon SE applications are structured.

📂 Project Structure
basic/
 ├── pom.xml
 └── src 
     ├── main
     │   ├── java/io/helidon/examples/webserver/basic/BasicMain.java
     │   └── resources/logging.properties
     └── test
         └── java/io/helidon/examples/webserver/basic/
             ├── AbstractBasicRoutingTest.java
             ├── BasicRoutingIT.java
             └── BasicRoutingTest.java

🧠 What This Example Shows
✔ Start a WebServer

The example configures and starts a Helidon SE WebServer on a selected port.

✔ Define Basic Routing

It demonstrates simple routing, including:

Returning plain text responses

Handling HTTP GET endpoints

Using routing builders

✔ Logging Configuration

logging.properties shows how Helidon configures Java Util Logging (JUL).

✔ Tests Included

The example contains:

Unit tests

Integration tests (running an actual WebServer instance)

These tests help verify routing behavior automatically.

▶️ How to Run
Using Maven

From the basic directory:

mvn package
java -jar target/basic.jar


Server starts on the default port (usually 8080).

You will see output like:

WEB server started at http://localhost:8080

🔗 Example Endpoints

Once the server is running, try:

GET /greet
curl http://localhost:8080/greet


Response:

Hello World!


If routing includes path parameters, you may also test:

GET /greet/{name}
curl http://localhost:8080/greet/Naveen


Example response:

Hello Naveen!

🧪 Run Tests
mvn test


This executes both:

Unit tests

Integration tests (start WebServer and verify responses)

📘 Useful to Learn

This example helps new users understand:

How to create a minimal Helidon SE service

How routing works

How requests and responses are handled

How Helidon SE applications are structured

How to write integration tests for WebServer applications