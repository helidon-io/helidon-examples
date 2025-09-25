
# Helidon Application Packaging Example

Helidon supports three packaging options for your application:

1. Thin Jar (the default)
2. JLink Custom Image
3. Native Image

It is also possible to package your Helidon application as a fat jar (although this is not recommended).

This examples shows how to build and run a Helidon application using these packaging options. 
It also shows how to build a file distribution for each of the formats.

## Build and Run

```shell
mvn package
java -jar target/helidon-examples-packaging.jar
```
## Exercise the application

```shell
curl -X GET http://localhost:8080/simple-greet
Hello World!
```

## Thin Jar

The default packaging for a Helidon application is the thin jar. In the above Build and Run
step you used a thin jar. In this packaging:

1. Your application code, and only your application code, is in the application jar (`target/helidon-examples-packaging.jar`).
2. The application's runtime dependencies are in the `target/libs` directory.
3. The application jar has entries in `META-INF/MANIFEST.MF` that specify:
   - The application's Main-Class
   - The Class-Path to use when running the application

To see how this looks in the `MANIFEST.MF` file, run:

```shell
unzip -p target/helidon-examples-packaging.jar META-INF/MANIFEST.MF
```

You'll see the `Class-Path` entry contains all jar files that are in the `libs` directory.

#### Thin Zip Distribution

To create a zip file of your thin jar application you can use zip:

```shell
(cd target; zip -r helidon-examples-packaging-thin.zip helidon-examples-packaging.jar libs/)
```

You can now copy this zip elsewhere and "install" and run it:

```shell
unzip helidon-examples-packaging.zip
java -jar helidon-examples-packaging.jar
```

As an alternative to the `zip` command this example also demonstrates how to use the
`maven-assembly-plugin` to create the distribution zip. This uses:

1. The `thin-zip` profile in the [pom.xml](./pom.xml) to configure the plugin.
2. The assembly descriptor in [thin-assembly.xml](./src/main/assembly/thin-assembly.xml)

To generate the zip using the `maven-assembly-plugin`:

```shell
mvn package -Pthin-zip
```
This will create `target/appplication-se-thin.zip` just like we did with the `zip` command.

## Jlink Image

The Jlink image creates a custom Java runtime image that is bundled with your application.
This custom runtime image contains only the JDK modules your application requires. It also
(by default) creates a CDS archive to speed up application launching.

To build and run:

```shell
mvn package -Pjlink-image
target/helidon-examples-packaging-jri/bin/start
```

If you look in `target/helidon-examples-packaging-jri` you will see:

1. `app`: this contains your application jar and dependencies, just like the thin jar case.
2. `bin`: this contains the start script for your application plus some JDK commands.
3. `lib/start.jsa`: this is a CDS archive for your application. It makes starting it a bit faster.
4. The rest of the files are the JDK files needed to run your application.

### JLink Zip Distribution

To create a zip file of your jlink application you can use zip:

```shell
(cd target; zip -r helidon-examples-packaging-jlink.zip helidon-examples-packaging-jri )
```

You can now copy this zip elsewhere and "install" and run it:

```shell
unzip helidon-examples-packaging-jlink.zip
helidon-examples-packaging-jri/bin/start
```

As an alternative to the `zip` command this example demonstrates how to use the
`maven-assembly-plugin` to create the distribution zip. This uses:

1. The `jlink-zip` profile in the [pom.xml](./pom.xml) to configure the plugin.
2. The assembly descriptor in [jlink-assembly.xml](./src/main/assembly/jlink-assembly.xml)

To generate the zip using the `maven-assembly-plugin`:

```shell
# This assume you previously built the image with -Pjlink-image
mvn package -Pjlink-zip
```
This will create `target/appplication-se-jlink.zip` just like we did with the `zip` command.

## Native Image

Native image creates a native executable of your Java application. You will need
Oracle GraalVM 21 installed on your system and set `GRAALVM_HOME` to point to your
installation. You can verify it by doing `${GRAALVM_HOME}/bin/native-image --version`.

To build and run:

```shell
mvn package -Pnative-image
target/helidon-examples-packaging
```

Your application is a native executable. You can see that by running:

```shell
file target/helidon-examples-packaging
```

### Native image Distribution

Your application is a single executable file so no distribution archive is required.

## Fat Jar

Fat jars are application jars that contain your application code plus all of its runtime dependencies.
Fat jars are not recommended because they require either merging of jar files (the basic
form of a fat jar) or a special class loader to handle the uber jar variant (which is a jar of jars).
Both of these add complexity.

Flattening of jars is also problematic because it can significantly alter the behavior
of your program in non-obvious ways. For example in an MP application if
bean-discovery-mode="all" is used, all classes from all jars would be
discovered as beans.

That said, it is possible to create a fat Jar for your Helidon application.

#### Fat Jar Distribution

This example uses the `maven-assembly-plugin` to create a fat jar. Specifically see:

1. The `fat-jar` profile in the [pom.xml](./pom.xml) to configure the plugin.
2. The assembly descriptor in [fat-assembly.xml](./src/main/assembly/fat-assembly.xml). 

To generate the fat jar:

```shell
mvn package -Pfat-jar
```

This will create `target/helidon-examples-packaging-fat.jar` which can be run with:

```shell
java -jar target/helidon-examples-packaging-fat.jar
```

