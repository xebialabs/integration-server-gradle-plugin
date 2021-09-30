package ai.digital.integration.util

import ai.digital.integration.server.util.DbParameters
import ai.digital.integration.server.util.DbUtil
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class DbUtilTest {

    @Test
    void isDerbyTest() {
        assertEquals(true, DbUtil.isDerby("derby"))
        assertEquals(true, DbUtil.isDerby("derby-network"))
        assertEquals(true, DbUtil.isDerby("derby-inmemory"))
        assertEquals(false, DbUtil.isDerby("mysql"))
    }

    @Test
    void detectDbDependenciesTest() {
        detectDbDependenciesDerbyNetwork("derby")
        detectDbDependenciesDerby()
        detectDbDependenciesDerbyNetwork("derby-network")
        detectDbDependenciesMssql()
        detectDbDependenciesMySql("mysql")
        detectDbDependenciesMySql("mysql-8")
        detectDbDependenciesOracle()
        detectDbDependenciesPostgres("postgres-10")
        detectDbDependenciesPostgres("postgres-12")
        detectDbDependenciesDerbyNetwork("default")
    }

    private void detectDbDependenciesDerbyNetwork(dbName) {
        DbParameters dbParameters = DbUtil.detectDbDependencies(dbName)
        assertEquals("org.apache.derby:derbyclient", dbParameters.getDriverDependency())
        assertEquals(null, dbParameters.getDriverClass())
        assertEquals(null, dbParameters.getDataFactory())
        assertEquals(null, dbParameters.getMetaFactory())
        assertEquals("\"?\"", dbParameters.getEscapePattern())
    }
    private void detectDbDependenciesDerby() {
        DbParameters dbParameters = DbUtil.detectDbDependencies("derby-inmemory")
        assertEquals("org.apache.derby:derby", dbParameters.getDriverDependency())
        assertEquals(null, dbParameters.getDriverClass())
        assertEquals(null, dbParameters.getDataFactory())
        assertEquals(null, dbParameters.getMetaFactory())
        assertEquals("\"?\"", dbParameters.getEscapePattern())
    }
    private void detectDbDependenciesMssql() {
        DbParameters dbParameters = DbUtil.detectDbDependencies("mssql")
        assertEquals("com.microsoft.sqlserver:mssql-jdbc", dbParameters.getDriverDependency())
        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", dbParameters.getDriverClass())
        assertEquals("org.dbunit.ext.mssql.MsSqlDataTypeFactory", dbParameters.getDataFactory())
        assertEquals(null, dbParameters.getMetaFactory())
        assertEquals("\"?\"", dbParameters.getEscapePattern())
    }
    private void detectDbDependenciesMySql(dbName) {
        DbParameters dbParameters = DbUtil.detectDbDependencies(dbName)
        assertEquals("mysql:mysql-connector-java", dbParameters.getDriverDependency())
        assertEquals("com.mysql.jdbc.Driver", dbParameters.getDriverClass())
        assertEquals("org.dbunit.ext.mysql.MySqlDataTypeFactory", dbParameters.getDataFactory())
        assertEquals("org.dbunit.ext.mysql.MySqlMetadataHandler", dbParameters.getMetaFactory())
        assertEquals("`?`", dbParameters.getEscapePattern())
    }
    private void detectDbDependenciesOracle() {
        DbParameters dbParameters = DbUtil.detectDbDependencies("oracle-19c-se")
        assertEquals("com.oracle.database.jdbc:ojdbc11", dbParameters.getDriverDependency())
        assertEquals("oracle.jdbc.OracleDriver", dbParameters.getDriverClass())
        assertEquals("org.dbunit.ext.oracle.OracleDataTypeFactory", dbParameters.getDataFactory())
        assertEquals(null, dbParameters.getMetaFactory())
        assertEquals("\"?\"", dbParameters.getEscapePattern())
    }
    private void detectDbDependenciesPostgres(dbName) {
        DbParameters dbParameters = DbUtil.detectDbDependencies(dbName)
        assertEquals("org.postgresql:postgresql", dbParameters.getDriverDependency())
        assertEquals("org.postgresql.Driver", dbParameters.getDriverClass())
        assertEquals("org.dbunit.ext.postgresql.PostgresqlDataTypeFactory", dbParameters.getDataFactory())
        assertEquals(null, dbParameters.getMetaFactory())
        assertEquals("\"?\"", dbParameters.getEscapePattern())
    }
}
