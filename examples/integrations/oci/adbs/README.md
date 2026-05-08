# Helidon Oracle Autonomous AI Database Serverless Example

This example consists of a simple command-line program that shows how Helidon can help with connecting your application
to an Oracle Autonomous AI Database Serverless instance running in the Oracle Cloud.

Normally connecting to an Oracle Autonomous AI Database Serverless instance requires a separate step [where client
credentials are
downloaded](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/connect-jdbc-thin-wallet.html#GUID-EBBB5D6B-118D-48BD-8D68-A3954EC92D2B)
and stored on the filesystem. This example performs this step once in memory as part of connecting.

The example consists of these parts and components:

* An
  [`oracle.jdbc.datasource.impl.OracleDataSource`](https://docs.oracle.com/en/database/oracle/oracle-database/26/jajdb/oracle/jdbc/datasource/impl/OracleDataSource.html)
  subclass (`io.helidon.examples.integrations.oci.adbs.AdbsDataSource`) that can automatically connect to an existing
  Oracle Autonomous AI Database Serverless instance running in the Oracle Cloud
    * This data source is useful enough that it may become a Helidon extension in the future.
* The [Oracle Universal Connection Pool](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjuar/index.html)
  which can use that data source
* A particular [meta-configuration](https://helidon.io/docs/latest/se/config/config-profiles#Profile-File) that
  integrates Helidon's [support for the Oracle Cloud Secret Management
  Service](https://docs.oracle.com/en-us/iaas/Content/secret-management/home.htm)
* Helidon's [support for named `DataSource`s](https://helidon.io/docs/latest/se/data#_helidon_config) that uses the
  Oracle Universal Connection Pool and Helidon Config

## Oracle Cloud Requirements

1. An Oracle Cloud
   [tenancy](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts-account.htm#concepttenancy) and a
   [compartment](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts-account.htm#conceptcompartment) in it.
2. An Oracle Cloud [Vault](https://docs.oracle.com/en-us/iaas/Content/KeyManagement/home.htm) in the compartment.
    1. Two [secrets](https://docs.oracle.com/en-us/iaas/Content/secret-management/home.htm) in that vault for your
       database's username and password:
       1. **`adbs.example.database.user`**: This secret should have the database username as its value.
       2. **`adbs.example.database.password`**: This secret should have the database password as its value.
3. An [Oracle Autonomous AI Database
   Serverless](https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/index.html) instance with [at least one
   user in
   it](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/manage-users-create.html#GUID-79BCDDA3-7B0B-47B5-B7A0-D9F155A6DB51).
4. [Policies](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts-account.htm#conceptpolicy) defined in the
   compartment to:
    1. permit a user to read from the database itself
    2. permit a user to [read some of the database's metadata concerning
       wallets](https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/connect-download-wallet.html#GUID-DED75E69-C303-409D-9128-5E10ADD47A35)
    3. permit a user to read the vault's secrets
5. A [`~/.oci/config` file](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm), or [other
   authentication mechanism supported by
   Helidon](https://helidon.io/docs/latest/se/integrations/oci#_configuring_authentication), configured appropriately to
   connect to your tenancy.

## Environment Variables

This example is designed so that you can run it by supplying your personal Oracle Cloud-related information as
environment variables. That way you don't have to edit `src/main/resources/application.yaml` or
`src/main/resources/meta-config.yaml`, since environment variables have a higher precedence in this example (though you
may if you wish). You'll need to define the following environment variables in the environment where you will run the
example:

1. **`COMPARTMENT_OCID`**: You'll set this variable to the value of your compartment's OCID. You can find its OCID in the
   OCI console. (This is needed by the OCI Vault and Secrets Management APIs.)
2. **`DATABASE_OCID`**: You'll set this variable to the value of your Oracle Autonomous AI Database Serverless instance's
   OCID. You can find its OCID in the OCI console.
3. **`VAULT_OCID`**: You'll set this variable to the value of your vault's OCID. You can find its OCID in the OCI console.

See also:

* `src/main/resources/meta-config.yaml`
* `src/main/resources/application.yaml`
* [Helidon Config documentation](https://helidon.io/docs/latest/se/config/introduction#_configuration)

## Building and Running

To build the example:

```shell
mvn package
```

To run the built example (remember to set the required environment variables first; see above):

```shell
java -jar ./target/helidon-examples-integrations-oci-adbs.jar
```

The program will run. The first acquisition of a connection will take some time as the wallet is automatically
downloaded and secrets are accessed. All subsequent connection acquisitions will be fast.

If successful, the program will print the results of `SELECT`ing "`Hello, world!`" from the Oracle Autonomous AI
Database Serverless instance running in the Oracle Cloud.

If there is a failure, begin by ensuring that you have created the necessary Oracle Cloud resources, and supplied the
proper Oracle Cloud information as detailed above.
