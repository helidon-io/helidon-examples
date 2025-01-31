# WebServer Custom Protocol Example

This example implements a custom connection handler to support a custom network protocol.

## Build and run

```bash
mvn package
java -jar target/helidon-examples-webserver-protocols-custom.jar
```

## Exercise the application

This example invents a simple network protocol called SDP (Simple Demo Protocol). SDP is a very simple, text based protocol. SDP requires that the first three characters on a new socket connection are `sdp` followed by an EOL. You can then send text commands to cause different responses from the server.

Since SDP is simple and text based you can use `telnet` or `nc` as a client.

The server's port (8080) supports both HTTP and SDP. To see this first send an HTTP request:
```
curl -X GET http://localhost:8080/
Hello from WebServer!
```

Next try SDP. Connect with `telnet`
```
telnet localhost 8080
```

Or `nc`:
```
nc -c localhost 8080
```

Type `sdp` followed by `<return>` . These three bytes and the end-of-line indicate that we are using the SDP protocol.
```
sdp
```

You will then get a `> ` prompt. Enter `help` to see what you can do:

```
> help
gc               Run GC
getProperties    Display all system properties
.                Exit
> 
```

Try entering the commands. When you are done enter `.` to terminate your request.

## The Code

To implement a custom protocol you need at a minimum two classes:

1. One (`SdpConnectionSelector`) that implements `ServerConnectionSelector`. This class is responsible for:
   1. Sniffing the first few bytes of an incoming request and determining if it is a protocol it handles.
   2. Creating a new `ServerConnection` to handle the incoming request.
2. One (`SdpConnection`) that implements `ServerConnection` to handle the incoming request. This is the class that implements the protocol.
