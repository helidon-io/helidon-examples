# Mutual TLS Example

This application demonstrates use of client certificates to 
authenticate HTTP client.

## Build and run

This example requires two components - the server and the client.

For each, there is an example using configuration, and an example using
builder APIs.

To test the example:
Start one of
 - `ServerBuilderMain` - main class for WebServer using builders
 - `ServerConfigMain`  - main class for WebServer using Config
 
Once the server is running, use one of:
 - `ClientBuilderMain` - main class for WebClient using builders
 - `ClientConfigMain`  - main class for WebClient using Config  

to invoke the endpoint using client certificate.

Alternative approach is to install the private key and certificate
to your browser and invoke the endpoint manually.  

### Howto regenerate certificates (optional)
In order to regenerate bundled certificates: client.p12 and server.p12 use
bundled script with given parameters:
```bash
./automatic-store-generator.sh --name Helidon --type P12 --single true
```
 and then copy generated certificates from ``out/client.p12`` and ``out/server.p12``
 to `src/main/resources` other generated files can be deleted.