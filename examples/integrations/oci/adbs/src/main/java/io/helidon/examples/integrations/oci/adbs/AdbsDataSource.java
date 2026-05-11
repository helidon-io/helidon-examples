/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.examples.integrations.oci.adbs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.helidon.service.registry.Services;

import com.oracle.bmc.OCID;
import com.oracle.bmc.database.Database;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.requests.GenerateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import oracle.jdbc.datasource.impl.OracleDataSource;
import oracle.net.jdbc.nl.NLException;
import oracle.net.jdbc.nl.NLParamParser;
import oracle.security.pki.OraclePKIProvider;
import oracle.security.pki.OracleWallet;

import static java.nio.charset.CodingErrorAction.REPORT;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNullElse;

/**
 * An {@link OracleDataSource} that {@linkplain #getConnection() supplies physical <code>Connection</code>s} to an <a
 * href="https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/index.html">Oracle Autonomous AI Database
 * Serverless</a> instance running in the <a href="https://cloud.oracle.com/">Oracle Cloud</a>, with automatic <a
 * href="https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/connect-download-wallet.html#GUID-DED75E69-C303-409D-9128-5E10ADD47A35">wallet
 * management</a> built in.
 *
 * @see #setDatabaseOcid(String)
 * @see #getConnection()
 */
public final class AdbsDataSource extends OracleDataSource {

    /**
     * The version of this class for serialization purposes.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Oracle Autonomous AI Database Serverless OCID.
     */
    private String databaseOcid;

    /**
     * The preferred suffix to help identify a database service name.
     */
    private String databaseServiceNameSuffix;

    /**
     * Whether this {@link AdbsDataSource} has self-configured.
     */
    private volatile boolean configured;

    /**
     * Creates a new {@link AdbsDataSource}.
     *
     * <p>All {@link javax.sql.DataSource} implementations must have a {@code public}, zero-argument constructor.</p>
     *
     * @throws SQLException if a database error occurs
     */
    public AdbsDataSource() throws SQLException {
        super();
        this.databaseServiceNameSuffix = "_tp";
    }

    /**
     * Returns the OCID for an Oracle Autonomous AI Database Serverless instance.
     *
     * @return the OCID for an Oracle Autonomous AI Database Serverless instance
     */
    public String getDatabaseOcid() {
        String databaseOcid = this.databaseOcid;
        if (databaseOcid == null) {
            try {
                databaseOcid = this.getURL();
            } catch (SQLException e) {
                // OracleDataSource tries to synthesize a URL when none has been set; this can fail; here we don't care.
            }
            if (databaseOcid != null) {
                if (OCID.isValid(databaseOcid)) {
                    this.databaseOcid = databaseOcid;
                } else {
                    databaseOcid = null;
                }
            }
        }
        return databaseOcid;
    }

    /**
     * Sets the OCID for an Oracle Autonomous AI Database Serverless instance.
     *
     * <p>All {@link javax.sql.DataSource} implementations must expose their configurable properties as Java Beans-style
     * properties. Ordinarily, this method should be called exactly once during configuration and/or setup.</p>
     *
     * @param databaseOcid the OCID for an Oracle Autonomous AI Database Serverless instance
     * @throws IllegalArgumentException if {@code databaseOcid} is non-{@code null} and not a {@linkplain
     * OCID#isValid(String) valid OCID}
     * @see OCID#isValid(String)
     */
    public void setDatabaseOcid(String databaseOcid) {
        if (databaseOcid != null && !OCID.isValid(databaseOcid)) {
            throw new IllegalArgumentException("databaseOcid: " + databaseOcid);
        }
        this.databaseOcid = databaseOcid;
    }

    /**
     * Returns the preferred suffix to help identify a database service name.
     *
     * @return the preferred suffix to help identify a database service name; never {@code null}
     */
    public String getDatabaseServiceNameSuffix() {
        return this.databaseServiceNameSuffix;
    }

    /**
     * Sets the preferred suffix to help identify a database service name.
     *
     * @param suffix the suffix; normally begins with {@code _}; must be, case insensitively, <a
     * href="https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/predefined-database-services-names.html">one
     * of the suffixes defined by the Oracle Autonomous Database Service</a>; if {@code null}, then {@code _tp} will be used
     */
    public void setDatabaseServiceNameSuffix(String suffix) {
        this.databaseServiceNameSuffix = requireNonNullElse(suffix, "_tp");
    }

    @Override // OracleDataSource
    public Connection getConnection() throws SQLException {
        this.ensureConfigured();
        return super.getConnection();
    }

    @Override // OracleDataSource
    public Connection getConnection(String username, String password) throws SQLException {
        this.ensureConfigured();
        return super.getConnection(username, password);
    }

    private void ensureConfigured() throws SQLException {
        if (!this.configured) {
            String databaseOcid = this.getDatabaseOcid();
            if (databaseOcid != null) {
                try {
                    this.install(databaseOcid, this.getDatabaseServiceNameSuffix());
                } catch (IOException
                         | KeyManagementException
                         | KeyStoreException
                         | NoSuchAlgorithmException
                         | NLException
                         | UnrecoverableKeyException e) {
                    throw new SQLException(e);
                }
            }
            this.configured = true;
        }
    }

    private void install(String ocid, String databaseServiceNameSuffix)
        throws IOException,
               KeyManagementException,
               KeyStoreException,
               NoSuchAlgorithmException,
               NLException,
               SQLException,
               UnrecoverableKeyException {
        this.install(this.wallet(ocid), databaseServiceNameSuffix);
    }

    private void install(GenerateAutonomousDatabaseWalletResponse response, String databaseServiceNameSuffix)
        throws IOException,
               KeyManagementException,
               KeyStoreException,
               NoSuchAlgorithmException,
               NLException,
               SQLException,
               UnrecoverableKeyException {
        if (response.getContentLength() <= 0) {
            return;
        }
        if (Security.getProvider("OraclePKI") == null) {
            // This provider can read cwallet.sso.
            Security.addProvider(new OraclePKIProvider());
        }
        String url = null;
        SSLContext sslContext = null;
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(response.getInputStream()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                try {
                    switch (entry.getName()) {
                        // See
                        // https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/connect-download-wallet.html#d46713e336
                    case "cwallet.sso":
                        sslContext = this.sslContext(zis.readAllBytes());
                        break;
                    case "tnsnames.ora":
                        // See
                        // https://docs.oracle.com/en/database/oracle/oracle-database/26/netrf/syntax-rules-configuration-files.html
                        url = this.url(zis.readAllBytes(), databaseServiceNameSuffix, this::setTNSEntryName);
                        break;
                    default:
                        break;
                    }
                } finally {
                    zis.closeEntry();
                }
            }
        }
        this.install(url, sslContext);
    }

    private void install(String url, SSLContext sslContext) throws SQLException {
        this.setURL(url);
        this.setSSLContext(sslContext);
    }

    private SSLContext sslContext(byte[] cwalletSsoBytes)
        throws IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        OracleWallet wallet = new OracleWallet();
        wallet.setWalletArray(cwalletSsoBytes, null); // null == deliberately no password
        return sslContext(wallet.getKeyStore());
    }

    private String url(byte[] tnsnamesOraBytes,
                       String databaseServiceNameSuffix,
                       Consumer<? super String> tnsNameConsumer) throws IOException, NLException {
        try (Reader tnsnamesOraReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(tnsnamesOraBytes),
                                                                                 US_ASCII.newDecoder()
                                                                                 .onMalformedInput(REPORT)
                                                                                 .onUnmappableCharacter(REPORT)))) {
            return this.url(tnsnamesOraReader, databaseServiceNameSuffix, tnsNameConsumer);
        }
    }

    private String url(Reader tnsnamesOraReader,
                       String databaseServiceNameSuffix,
                       Consumer<? super String> tnsNameConsumer) throws IOException, NLException {
        return this.url(new NLParamParser(tnsnamesOraReader), databaseServiceNameSuffix, tnsNameConsumer);
    }

    private String url(NLParamParser tnsnamesOraParser,
                       String databaseServiceNameSuffix,
                       Consumer<? super String> tnsNameConsumer) throws NLException {
        int suffixLength = databaseServiceNameSuffix.length();
        // See https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/predefined-database-services-names.html
        for (String databaseServiceName : tnsnamesOraParser.getNLPAllNames()) {
            // The parser happens to uppercase everything. So we need to do case-insensitive matching.
            //
            // If "myDATABasE_MEDiUM" ends (case-insensitively) with "_mediUm"...
            if (databaseServiceName.regionMatches(true,
                                                  databaseServiceName.length() - suffixLength,
                                                  databaseServiceNameSuffix,
                                                  0,
                                                  suffixLength)) {
                tnsNameConsumer.accept(databaseServiceName);
                return "jdbc:oracle:thin:@" + tnsnamesOraParser.getNLPListElement(databaseServiceName).valueToString();
            }
        }
        return null;
    }

    private GenerateAutonomousDatabaseWalletResponse wallet(String ocid) {
        return this.wallet(Services.get(Database.class), ocid);
    }

    private GenerateAutonomousDatabaseWalletResponse wallet(Database database, String databaseOcid) {
        return database
            .generateAutonomousDatabaseWallet(GenerateAutonomousDatabaseWalletRequest.builder()
                                              .autonomousDatabaseId(databaseOcid)
                                              .generateAutonomousDatabaseWalletDetails(this.walletDetails(databaseOcid)) // <1>
                                              .build());
        // <1>: You need a wallet password to generate the wallet, even though you don't need one to read it for this
        // use case. So we re-use the database OCID for this.
    }

    private GenerateAutonomousDatabaseWalletDetails walletDetails(String walletPassword) {
        return GenerateAutonomousDatabaseWalletDetails.builder()
            .password(walletPassword)
            .build();
    }

    private static SSLContext sslContext(KeyStore keyStore)
        throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        // The default algorithm will be "PKIX"; see
        // https://docs.oracle.com/en/java/javase/26/docs/specs/security/standard-names.html#trustmanagerfactory-algorithms
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        // The default algorithm will be "PKIX"; see
        // https://docs.oracle.com/en/java/javase/26/docs/specs/security/standard-names.html#keymanagerfactory-algorithms
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore,
                               null); // deliberately no password for this KeyManagerFactory
        // See https://docs.oracle.com/en/java/javase/26/docs/specs/security/standard-names.html#sslcontext-algorithms.
        // We don't want to use getDefault() (which also uses "TLS") because we want a distinct instance, not the shared
        // one.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(),
                        trustManagerFactory.getTrustManagers(),
                        null); // use the default SecureRandom source
        return sslContext;
    }

}
