# Helidon MP Bean Validation Example

This example implements a simple Hello World REST service using MicroProfile demonstrating Bean Validation.

## Usage

To be able to use bean validation add the following dependency: 

```xml
<dependency>
    <groupId>io.helidon.microprofile.bean-validation</groupId>
    <artifactId>helidon-microprofile-bean-validation</artifactId>
</dependency>
```

## Build and run

```shell
mvn package
java -jar target/helidon-examples-microprofile-bean-validation.jar
```

## Exercise the application

```
curl -X GET http://localhost:8080/valid/<email>
{"message":"Valid <email>!"}

curl -X GET -I http://localhost:8080/valid/null


1 constraint violation(s) occurred during method validation.
Constructor or Method: public jakarta.json.JsonObject io.helidon.examples.microprofile.bean.validation.ValidEmailResource.getMessage(java.lang.String)
Argument values: [null]
Constraint violations: 
 (1) Kind: PARAMETER
 parameter index: 0
 message: must be a well-formed email address
 root bean: io.helidon.examples.microprofile.bean.validation.ValidEmailResource$Proxy$_$$_WeldSubclass@58f396f6
 property path: getMessage.arg0
 constraint: @jakarta.validation.constraints.Email(flags={}, groups={}, regexp=".*", message="{jakarta.validation.constraints.Email.message}", payload={})

```