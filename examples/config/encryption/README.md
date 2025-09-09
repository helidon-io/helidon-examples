# Storing Encrypted Values in Helidon Config

This example uses the `helidon-config-encryption` module to encrypt
a value so that it can be stored and retrieved from `application.yaml`.
It uses AES/GCM symmetric key encryption.

## Build and Run 

```bash
mvn clean package
java -jar target/helidon-examples-config-encryption.jar
```

The application prints:

```
SECRET!!! secret-key=secret-value
```

But the value is just clear text in the config file. How do we encrypt it?

## Encrypt value using AES/GCM

We encrypt the value using Helidon's encryption tooling.
We use AES/GCM symmetric encryption with the private key
(aka master password) `changeit`.

```base
java -jar target/libs/helidon-config-encryption-*.jar aes changeit secret-value
```
**Note**: for Helidon 4.2.1-4.2.6 you need to provide the classpath when running the command:
`java -cp "target/libs/*" io.helidon.config.encryption.Main aes changeit secret-value`

This will produce an encrypted value suitable for putting in a Helidon configuration file.
It will look something like:

```
${GCM=PAFWz...txS74=}
```

(the three dots above represent a lengthy string of random characters -- not literally three dots)

Now edit `src/main/resources/application.yaml` and replace `secret-value` with the encrypted value.
It will look something like this:

```
secret-key: "${GCM=PAFWz...txS74=}"
```

Now build and re-run your application. You will see the encrypted value returned
from Helidon Config because we have not provided the key to decrypt the value:

```
SECRET!!! secret-key=${GCM=PAF5IWz...txS74=}
```


## Passing the private key to your Helidon application

In this example we are going to pass the private key (master password) to the application using the special environment variable `SECURE_CONFIG_AES_MASTER_PWD`:

```bash
SECURE_CONFIG_AES_MASTER_PWD=changeit java -jar target/helidon-examples-config-encryption.jar
```

So now you see the decrypted value:

```
SECRET!!! secret-key=secret-value
```

## What is happening

The `helidon-config-encryption` module provides config encryption support. When it is added to your
project as a dependency it registers a `ConfigFilter` that understands how to decrypt
encrypted values. This happens automatically when you retrieve the value of an encrypted configuration property.
The [EncryptionFilter](https://helidon.io/docs/latest/apidocs/io.helidon.config.encryption/io/helidon/config/encryption/EncryptionFilter.html) 
knows to check the environment variable for the private key.
