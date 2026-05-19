# Helidon Oracle Autonomous AI Database Serverless Example Using Token-Based Authentication

This example consists of a simple command-line program that shows how Helidon can help with connecting your application
to an Oracle Autonomous AI Database Serverless instance running in the Oracle Cloud using IAM authentication by using
official, open-source [Oracle OJDBC
extensions](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#oracle-jdbc-providers-for-oci).

The example consists of these parts and components:

* A main class that exercises the example.
* Helidon's [support for named `DataSource`s](https://helidon.io/docs/latest/se/data#_helidon_config) that uses the
  Oracle Universal Connection Pool and Helidon Config
* A `src/main/resources/application.yaml` file that configures the example.
* Usage, via configuration, of the [Oracle Universal Connection
  Pool](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjuar/index.html).
* Usage, via configuration, of the [Oracle OJDBC OCI
  Provider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#oracle-jdbc-providers-for-oci)
  project. From that project:
    * Usage, via configuration, of the [Access Token
      {rovider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#access-token-provider)
    * Usage, via configuration, of the [Database Connection String
      Provider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#database-connection-string-provider)

## Oracle Cloud Requirements

1. An Oracle Cloud
   [tenancy](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts-account.htm#concepttenancy) and a
   [compartment](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts-account.htm#conceptcompartment) in it.
3. An [Oracle Autonomous AI Database
   Serverless](https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/index.html) instance with [at least one
   user in
   it](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/manage-users-create.html#GUID-79BCDDA3-7B0B-47B5-B7A0-D9F155A6DB51).
    1. The database must be configured to [allow walletless TLS connections](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/autonomous-provision.html)
    2. The database must be configured to allow [IAM authentication](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/iam-access-database.html#GUID-CFC74EAF-E887-4B1F-9E9A-C956BCA0BEA9)
4. [Policies](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts-account.htm#conceptpolicy) defined in the
   compartment to:
    1. permit a user to `use` `autonomous-database-family` in the database's compartment
5. A [`~/.oci/config` file](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm), or [other
   authentication mechanism supported by the OJDBC OCI
   Provider](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#configuring-authentication-1),
   [configured](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#common-parameters-for-resource-providers)
   appropriately, used to connect to your tenancy.

## Environment Variables

This example is designed so that you can run it by supplying your personal Oracle Cloud-related information as
environment variables. That way you don't have to edit `src/main/resources/application.yaml`, since environment
variables have a higher precedence in this example (though you may if you wish). You'll need to define the following
environment variables in the environment where you will run the example:

1. **`COMPARTMENT_OCID`**: You'll set this variable to the value of your compartment's OCID. You can find its OCID in
   the OCI console. (This is needed to [scope your access token
   appropriately](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#configuring-a-scope).)
2. **`DATABASE_OCID`**: You'll set this variable to the value of your Oracle Autonomous AI Database Serverless instance's
   OCID. You can find its OCID in the OCI console.

See also:

* `src/main/resources/application.yaml`
* [Helidon Config documentation](https://helidon.io/docs/latest/se/config/introduction#_configuration)

## Building and Running

To build the example:

```shell
mvn package
```

To run the built example (remember to set the required environment variables first; see above):

```shell
java -jar ./target/helidon-examples-integrations-oci-adbs-iam.jar
```

The program will run. The first acquisition of a connection will take some time as the initial connection is negotiated.
All subsequent connection acquisitions will be fast. Access tokens are
[cached](https://github.com/oracle/ojdbc-extensions/tree/main/ojdbc-provider-oci#caching-mechanism) by default.

If successful, the program will print the results of `SELECT`ing "`Hello, world!`" from the Oracle Autonomous AI
Database Serverless instance running in the Oracle Cloud.

If there is a failure, begin by ensuring that you have created the necessary Oracle Cloud resources, and supplied the
proper Oracle Cloud information as detailed above.
