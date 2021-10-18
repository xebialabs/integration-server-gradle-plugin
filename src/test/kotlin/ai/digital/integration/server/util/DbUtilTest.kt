package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.DbUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DbUtilTest {

    @Test
    fun isDerbyTest() {
        assertEquals(true, DbUtil.isDerby("derby"))
        assertEquals(true, DbUtil.isDerby("derby-network"))
        assertEquals(true, DbUtil.isDerby("derby-inmemory"))
        assertEquals(false, DbUtil.isDerby("mysql"))
    }

    @Test
    fun detectDbDependenciesTest() {
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

    private fun detectDbDependenciesDerbyNetwork(dbName: String) {
        val dbParameters = DbUtil.detectDbDependencies(dbName)
        assertEquals("org.apache.derby:derbyclient", dbParameters.driverDependency)
        assertEquals(null, dbParameters.driverClass)
        assertEquals(null, dbParameters.dataFactory)
        assertEquals(null, dbParameters.metaFactory)
        assertEquals("\"?\"", dbParameters.escapePattern)
    }

    private fun detectDbDependenciesDerby() {
        val dbParameters = DbUtil.detectDbDependencies("derby-inmemory")
        assertEquals("org.apache.derby:derby", dbParameters.driverDependency)
        assertEquals(null, dbParameters.driverClass)
        assertEquals(null, dbParameters.dataFactory)
        assertEquals(null, dbParameters.metaFactory)
        assertEquals("\"?\"", dbParameters.escapePattern)
    }

    private fun detectDbDependenciesMssql() {
        val dbParameters = DbUtil.detectDbDependencies("mssql")
        assertEquals("com.microsoft.sqlserver:mssql-jdbc", dbParameters.driverDependency)
        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", dbParameters.driverClass)
        assertEquals("org.dbunit.ext.mssql.MsSqlDataTypeFactory", dbParameters.dataFactory)
        assertEquals(null, dbParameters.metaFactory)
        assertEquals("\"?\"", dbParameters.escapePattern)
    }

    private fun detectDbDependenciesMySql(dbName: String) {
        val dbParameters = DbUtil.detectDbDependencies(dbName)
        assertEquals("mysql:mysql-connector-java", dbParameters.driverDependency)
        assertEquals("com.mysql.jdbc.Driver", dbParameters.driverClass)
        assertEquals("org.dbunit.ext.mysql.MySqlDataTypeFactory", dbParameters.dataFactory)
        assertEquals("org.dbunit.ext.mysql.MySqlMetadataHandler", dbParameters.metaFactory)
        assertEquals("`?`", dbParameters.escapePattern)
    }

    private fun detectDbDependenciesOracle() {
        val dbParameters = DbUtil.detectDbDependencies("oracle-19c-se")
        assertEquals("com.oracle.database.jdbc:ojdbc11", dbParameters.driverDependency)
        assertEquals("oracle.jdbc.OracleDriver", dbParameters.driverClass)
        assertEquals("org.dbunit.ext.oracle.OracleDataTypeFactory", dbParameters.dataFactory)
        assertEquals(null, dbParameters.metaFactory)
        assertEquals("\"?\"", dbParameters.escapePattern)
    }

    private fun detectDbDependenciesPostgres(dbName: String) {
        val dbParameters = DbUtil.detectDbDependencies(dbName)
        assertEquals("org.postgresql:postgresql", dbParameters.driverDependency)
        assertEquals("org.postgresql.Driver", dbParameters.driverClass)
        assertEquals("org.dbunit.ext.postgresql.PostgresqlDataTypeFactory", dbParameters.dataFactory)
        assertEquals(null, dbParameters.metaFactory)
        assertEquals("\"?\"", dbParameters.escapePattern)
    }
}
