# Helidon MP TLS Example

This examples shows how to configure server TLS using Helidon MP.

Note: This example uses self-signed server certificate!

### How to generate self-signed certificate (optional) 
In this example the certificate is bundled so no special certificate is required.
Required tools: keytool
```bash
keytool -genkeypair -keyalg RSA -keysize 2048 -alias server -dname "CN=localhost" -validity 21650 -keystore server.jks -storepass changeit -keypass changeit -deststoretype pkcs12
keytool -exportcert -keystore server.jks -storepass changeit -alias server -rfc -file server.cer
keytool -certreq -keystore server.jks -alias server -keypass changeit -storepass changeit -keyalg rsa -file server.csr
keytool -importkeystore -srckeystore server.jks -destkeystore server.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass changeit -deststorepass changeit
```

## Build and run

```bash
mvn package
java -jar target/helidon-examples-microprofile-tls.jar
```
## Exercise the application
```bash
curl -k -X GET https://localhost:8080
```
