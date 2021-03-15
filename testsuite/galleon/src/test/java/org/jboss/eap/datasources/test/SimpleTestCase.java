/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.eap.datasources.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import javax.inject.Inject;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import org.wildfly.core.testrunner.ManagementClient;
import org.wildfly.core.testrunner.WildflyTestRunner;

@RunWith(WildflyTestRunner.class)
public class SimpleTestCase {

    private static final String EAP_WITH_ALL_ENV_TEST = "EAP_WITH_ALL_ENV_TEST";
    private static final String EAP_WITH_ENV_TEST = "EAP_WITH_ENV_TEST";

    private static final String MARIADB_PREFIX = "MARIADB";
    private static final String ORACLE_PREFIX = "ORACLE";
    private static final String POSTGRESQL_PREFIX = "POSTGRESQL";

    private static final Map<String, String> ENV_VARIABLES_PREFIXES = new HashMap<>();
    private static final Map<String, String> DS_TO_PREFIX = new HashMap<>();
    private static final Map<String, String> ENV_TO_ATTRIBUTE = new HashMap<>();

    @Inject
    private ManagementClient managementClient;
    private static final String MARIADB_DS = "MariaDBDS";
    private static final String ORACLE_DS = "OracleDS";
    private static final String POSTGRESQL_DS = "PostgreSQLDS";

    private static final String PLACE_HOLDER = "XXX";

    private static final String[] DATASOURCES = {MARIADB_DS, ORACLE_DS, POSTGRESQL_DS};
    // These databases have host,port,database properties for connection-url
    private static final String[] DATASOURCES_WITH_HPD = {MARIADB_DS, POSTGRESQL_DS};

    private static final String[] DRIVERS = {"mariadb", "oracle", "postgresql"};
    private static final String DATASOURCES_ADDRESS = "/subsystem=datasources/";

    private static final Map<String, Map<String, String>> SPECIFIC_DEFAULT_VALUES = new HashMap<>();
    private static final Map<String, String> COMMON_DEFAULT_VALUES = new HashMap<>();

    private static final Map<String, String> SYSTEM_PROPERTIES_VALUES = new HashMap<>();
    private static final Map<String, String> DS_TO_SYSTEM_PROPERTY = new HashMap<>();

    private static final Map<String, String> CONNECTION_URL_PREFIX = new HashMap<>();

    static {

        DS_TO_SYSTEM_PROPERTY.put(MARIADB_DS, "mariadb");
        DS_TO_SYSTEM_PROPERTY.put(ORACLE_DS, "oracle");
        DS_TO_SYSTEM_PROPERTY.put(POSTGRESQL_DS, "postgresql");

        CONNECTION_URL_PREFIX.put(MARIADB_DS, "jdbc:mariadb://");
        CONNECTION_URL_PREFIX.put(POSTGRESQL_DS, "jdbc:postgresql://");

        COMMON_DEFAULT_VALUES.put("background-validation", "false");
        COMMON_DEFAULT_VALUES.put("background-validation-millis", "10000");
        COMMON_DEFAULT_VALUES.put("enabled", "true");
        COMMON_DEFAULT_VALUES.put("jta", "true");
        COMMON_DEFAULT_VALUES.put("max-pool-size", "20");
        COMMON_DEFAULT_VALUES.put("min-pool-size", "0");
        COMMON_DEFAULT_VALUES.put("statistics-enabled", "false");
        COMMON_DEFAULT_VALUES.put("transaction-isolation", "TRANSACTION_READ_COMMITTED");
        COMMON_DEFAULT_VALUES.put("validate-on-match", "true");
        COMMON_DEFAULT_VALUES.put("stale-connection-checker-class-name", "org.jboss.jca.adapters.jdbc.extensions.novendor.NullStaleConnectionChecker");
        COMMON_DEFAULT_VALUES.put("exception-sorter-class-name", "org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter");
        COMMON_DEFAULT_VALUES.put("valid-connection-checker-class-name", "org.jboss.jca.adapters.jdbc.extensions.novendor.NullValidConnectionChecker");
        COMMON_DEFAULT_VALUES.put("flush-strategy","FailingConnectionOnly");
        Map<String, String> mariadb = new HashMap<>();
        SPECIFIC_DEFAULT_VALUES.put(MARIADB_DS, mariadb);
        mariadb.put("connection-url", "jdbc:mariadb://${org.jboss.eap.datasources.mariadb.host,env.MARIADB_SERVICE_HOST,env.MARIADB_HOST}:${org.jboss.eap.datasources.mariadb.port,env.MARIADB_SERVICE_PORT,env.MARIADB_PORT}/${org.jboss.eap.datasources.mariadb.database,env.MARIADB_DATABASE}");
        mariadb.put("jndi-name", "java:jboss/datasources/MariaDBDS");
        mariadb.put("password", "${org.jboss.eap.datasources.mariadb.password,env.MARIADB_PASSWORD}");
        mariadb.put("user-name", "${org.jboss.eap.datasources.mariadb.user-name,env.MARIADB_USER}");

        Map<String, String> oracle = new HashMap<>();
        SPECIFIC_DEFAULT_VALUES.put(ORACLE_DS, oracle);
        oracle.put("connection-url", "${org.jboss.eap.datasources.oracle.connection-url,env.ORACLE_URL}");
        oracle.put("jndi-name", "java:jboss/datasources/OracleDS");
        oracle.put("password", "${org.jboss.eap.datasources.oracle.password,env.ORACLE_PASSWORD}");
        oracle.put("user-name", "${org.jboss.eap.datasources.oracle.user-name,env.ORACLE_USER}");

        Map<String, String> postgresql = new HashMap<>();
        SPECIFIC_DEFAULT_VALUES.put(POSTGRESQL_DS, postgresql);
        postgresql.put("connection-url", "jdbc:postgresql://${org.jboss.eap.datasources.postgresql.host,env.POSTGRESQL_SERVICE_HOST,env.POSTGRESQL_HOST}:${org.jboss.eap.datasources.postgresql.port,env.POSTGRESQL_SERVICE_PORT,env.POSTGRESQL_PORT}/${org.jboss.eap.datasources.postgresql.database,env.POSTGRESQL_DATABASE}");
        postgresql.put("jndi-name", "java:jboss/datasources/PostgreSQLDS");
        postgresql.put("password", "${org.jboss.eap.datasources.postgresql.password,env.POSTGRESQL_PASSWORD}");
        postgresql.put("user-name", "${org.jboss.eap.datasources.postgresql.user-name,env.POSTGRESQL_USER}");

        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".enabled", "false");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".exception-sorter-class-name", "foo");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".jndi-name", "bar");

        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".jta", "false");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".max-pool-size", "9999");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".min-pool-size", "55");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".connection-url", "foourl");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".user-name", "foouser");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".password", "foopassword");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".validate-on-match", "false");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".background-validation", "true");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".background-validation-millis", "0");
        SYSTEM_PROPERTIES_VALUES.put("wildfly.datasources.statistics-enabled", "true");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".stale-connection-checker-class-name", "foostale");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".valid-connection-checker-class-name", "foochecker");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".transaction-isolation", "TRANSACTION_SERIALIZABLE");
        SYSTEM_PROPERTIES_VALUES.put("org.jboss.eap.datasources." + PLACE_HOLDER + ".flush-strategy", "IdleConnections");

        ENV_VARIABLES_PREFIXES.put(MARIADB_PREFIX, MARIADB_DS);
        ENV_VARIABLES_PREFIXES.put(ORACLE_PREFIX, ORACLE_DS);
        ENV_VARIABLES_PREFIXES.put(POSTGRESQL_PREFIX, POSTGRESQL_DS);

        DS_TO_PREFIX.put(MARIADB_DS, MARIADB_PREFIX);
        DS_TO_PREFIX.put(ORACLE_DS, ORACLE_PREFIX);
        DS_TO_PREFIX.put(POSTGRESQL_DS, POSTGRESQL_PREFIX);

        ENV_TO_ATTRIBUTE.put("_ENABLED", "enabled");
        ENV_TO_ATTRIBUTE.put("_EXCEPTION_SORTER", "exception-sorter-class-name");
        ENV_TO_ATTRIBUTE.put("_JNDI", "jndi-name");
        ENV_TO_ATTRIBUTE.put("_JTA", "jta");
        ENV_TO_ATTRIBUTE.put("_MAX_POOL_SIZE", "max-pool-size");
        ENV_TO_ATTRIBUTE.put("_MIN_POOL_SIZE", "min-pool-size");
        ENV_TO_ATTRIBUTE.put("_URL", "connection-url");
        ENV_TO_ATTRIBUTE.put("_USER", "user-name");
        ENV_TO_ATTRIBUTE.put("_PASSWORD", "password");
        ENV_TO_ATTRIBUTE.put("_VALIDATE_ON_MATCH", "validate-on-match");
        ENV_TO_ATTRIBUTE.put("_BACKGROUND_VALIDATION", "background-validation");
        ENV_TO_ATTRIBUTE.put("_BACKGROUND_VALIDATION_MILLIS", "background-validation-millis");
        ENV_TO_ATTRIBUTE.put("_STALE_CONNECTION_CHECKER", "stale-connection-checker-class-name");
        ENV_TO_ATTRIBUTE.put("_CONNECTION_CHECKER", "valid-connection-checker-class-name");
        ENV_TO_ATTRIBUTE.put("_TX_ISOLATION", "transaction-isolation");
        ENV_TO_ATTRIBUTE.put("_FLUSH_STRATEGY", "flush-strategy");

    }

    @Test
    public void test() throws IOException {
        String withAllEnv = System.getenv(EAP_WITH_ALL_ENV_TEST);
        if (withAllEnv == null) {
            String withEnv = System.getenv(EAP_WITH_ENV_TEST);
            if (withEnv == null) {
                testNoEnv();
            } else {
                testEnv();
            }
        } else {
            testAllEnv();
        }
    }

    // Test jndi-name and connection-url only
    private void testEnv() throws IOException {
        for (String datasource : DATASOURCES) {
            String prefix = DS_TO_PREFIX.get(datasource);
            String value = System.getenv(prefix + "_DATASOURCE");
            System.out.println("VALUE " + value);
            String expectedValue = "java:jboss/datasources/" + value;
            ModelNode resource = check("data-source=" + datasource);
            String attribute = "jndi-name";
            assertEquals(datasource + ":" + attribute, expectedValue, resource.get(attribute).asString());
        }
        for (String datasource : DATASOURCES_WITH_HPD) {
            String prefix = DS_TO_PREFIX.get(datasource);
            String connectionPrefix = CONNECTION_URL_PREFIX.get(datasource);
            String hostValue = System.getenv(prefix + "_HOST");
            hostValue = hostValue == null ? System.getenv(prefix + "_SERVICE_HOST") : hostValue;
            String portValue = System.getenv(prefix + "_PORT");
            portValue = portValue == null ? System.getenv(prefix + "_SERVICE_PORT") : portValue;
            String databaseValue = System.getenv(prefix + "_DATABASE");
            String attribute = "connection-url";
            String expectedValue = connectionPrefix + hostValue + ":" + portValue + "/" + databaseValue;
            ModelNode resource = check("data-source=" + datasource);
            assertEquals(datasource + ":" + attribute, expectedValue, resource.get(attribute).asString());
        }
        // System properties take precedence
        for (String datasource : DATASOURCES) {
            checkWithSystemProperties(datasource);
        }
    }

    private Map<String, Map<String, String>> getValues() {
        Map<String, String> envs = System.getenv();
        Map<String, Map<String, String>> expectedValues = new HashMap<>();
        for (Entry<String, String> entry : envs.entrySet()) {
            String key = entry.getKey();
            for (String prefix : ENV_VARIABLES_PREFIXES.keySet()) {
                if (key.startsWith(prefix)) {
                    String datasource = ENV_VARIABLES_PREFIXES.get(prefix);
                    Map<String, String> values = expectedValues.get(datasource);
                    if (values == null) {
                        values = new HashMap<>();
                        expectedValues.put(datasource, values);
                    }
                    String attributeId = key.substring(prefix.length());
                    String attribute = ENV_TO_ATTRIBUTE.get(attributeId);
                    values.put(attribute, entry.getValue());
                }
            }
        }
        return expectedValues;
    }

    private void testAllEnv() throws IOException {
        Map<String, Map<String, String>> expectedValues = getValues();
        for (String datasource : DATASOURCES) {
            Map<String, String> datasourceValues = expectedValues.get(datasource);
            if (datasourceValues == null) {
                throw new RuntimeException("Invalid ENV variables, no values for " + datasource);
            } else {
                if (datasourceValues.isEmpty()) {
                    throw new RuntimeException("Invalid ENV variables, no values for " + datasource);
                }
                ModelNode resource = check("data-source=" + datasource);
                for (Entry<String, String> val : datasourceValues.entrySet()) {
                    assertEquals(datasource + ":" + val.toString(), val.getValue(), resource.get(val.getKey()).asString());
                }
            }
        }
        // System properties take precedence
        for (String datasource : DATASOURCES) {
            checkWithSystemProperties(datasource);
        }
    }

    private void testNoEnv() throws IOException {
        for (String datasource : DATASOURCES) {
            checkDS(datasource);
        }

        for (String driver : DRIVERS) {
            checkDriver(driver);
        }

        testWithSystemProperties();
    }

    private void testWithSystemProperties() throws IOException {
        // Set System properties and check
        for (String datasource : DATASOURCES) {
            checkWithSystemProperties(datasource);
        }

        // Special cases for jndi-name that cascades onto 2 system properties
        for (String datasource : DATASOURCES) {
            String db = DS_TO_SYSTEM_PROPERTY.get(datasource);
            String key = "org.jboss.eap.datasources." + db + ".datasource";
            String value = "foods";
            String attribute = "jndi-name";
            addProperty(key, value);
            String expectedValue = "java:jboss/datasources/" + value;
            ModelNode resource = check("data-source=" + datasource);
            assertEquals(datasource + ":" + attribute, expectedValue, resource.get(attribute).asString());
            removeProperty(key);
        }

        // Finally, some db have host, port and database for connection-url
        for (String datasource : DATASOURCES_WITH_HPD) {
            String db = DS_TO_SYSTEM_PROPERTY.get(datasource);
            String prefix = CONNECTION_URL_PREFIX.get(datasource);
            String host = "org.jboss.eap.datasources." + db + ".host";
            String hostValue = "foo";
            String port = "org.jboss.eap.datasources." + db + ".port";
            String portValue = "999";
            String database = "org.jboss.eap.datasources." + db + ".database";
            String databaseValue = "myDB";
            String attribute = "connection-url";
            addProperty(host, hostValue);
            addProperty(port, portValue);
            addProperty(database, databaseValue);
            String expectedValue = prefix + hostValue + ":" + portValue + "/" + databaseValue;
            ModelNode resource = check("data-source=" + datasource);
            assertEquals(datasource + ":" + attribute, expectedValue, resource.get(attribute).asString());

            removeProperty(host);
            removeProperty(port);
            removeProperty(database);
        }
    }

    private void checkWithSystemProperties(String ds) throws IOException {
        String db = DS_TO_SYSTEM_PROPERTY.get(ds);
        Map<String, String> newExpectedValues = new HashMap<>();
        Set<String> properties = new HashSet<>();
        for (Entry<String, String> entry : SYSTEM_PROPERTIES_VALUES.entrySet()) {
            String key = entry.getKey();
            String attribute = key.substring(key.lastIndexOf(".") + 1, key.length());
            key = key.replace(PLACE_HOLDER, db);
            properties.add(key);
            addProperty(key, entry.getValue());
            newExpectedValues.put(attribute, entry.getValue());
        }
        ModelNode resource = check("data-source=" + ds);
        for (Entry<String, String> entry : newExpectedValues.entrySet()) {
            assertEquals(ds + ":" + entry.toString(), entry.getValue(), resource.get(entry.getKey()).asString());
        }
        for (String p : properties) {
            removeProperty(p);
        }
    }

    private void checkDS(String ds) throws IOException {
        ModelNode resource = check("data-source=" + ds);
        for (Entry<String, String> entry : COMMON_DEFAULT_VALUES.entrySet()) {
            assertEquals(ds + ":" + entry.toString(), entry.getValue(), resource.get(entry.getKey()).asString());
        }
        Map<String, String> specifics = SPECIFIC_DEFAULT_VALUES.get(ds);
        for (Entry<String, String> entry : specifics.entrySet()) {
            assertEquals(ds + ":" + entry.toString(), entry.getValue(), resource.get(entry.getKey()).asString());
        }
    }

    private void checkDriver(String driver) throws IOException {
        check("jdbc-driver=" + driver);
    }

    private ModelNode check(String address) throws IOException {
        ModelControllerClient client = managementClient.getControllerClient();
        final ModelNode operation = new ModelNode();
        operation.get("operation").set("read-resource");
        operation.get("resolve-expressions").set(true);
        operation.get("address").set(DATASOURCES_ADDRESS + address);
        ModelNode result = client.execute(operation);
        assertEquals(result.asString(), "success", result.get("outcome").asString());
        return result.get("result");
    }

    private void addProperty(String key, String value) throws IOException {
        ModelControllerClient client = managementClient.getControllerClient();
        final ModelNode operation = new ModelNode();
        operation.get("operation").set("add");
        operation.get("value").set(value);
        operation.get("address").set("/system-property=" + key);
        ModelNode result = client.execute(operation);
        assertEquals(result.asString(), "success", result.get("outcome").asString());
    }

    private void removeProperty(String key) throws IOException {
        ModelControllerClient client = managementClient.getControllerClient();
        final ModelNode operation = new ModelNode();
        operation.get("operation").set("remove");
        operation.get("address").set("/system-property=" + key);
        ModelNode result = client.execute(operation);
        assertEquals(result.asString(), "success", result.get("outcome").asString());
    }
}
