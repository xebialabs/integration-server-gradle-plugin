package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.DeployIntegrationServerExtension
import ai.digital.integration.server.common.domain.Database
import ai.digital.integration.server.common.domain.DbParameters
import ai.digital.integration.server.common.util.HTTPUtil.Companion.findFreePort
import com.fasterxml.jackson.core.TreeNode
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.InputStream
import java.nio.file.Path

class DbUtil {

    companion object {
        @JvmStatic
        val POSTGRES = "postgres-10"

        @JvmStatic
        val POSTGRES12 = "postgres-12"

        @JvmStatic
        val ORACLE19 = "oracle-19c-se"

        @JvmStatic
        val MYSQL = "mysql"

        @JvmStatic
        val MYSQL8 = "mysql-8"

        @JvmStatic
        val MSSQL = "mssql"

        @JvmStatic
        val DERBY = "derby"

        @JvmStatic
        val DERBY_NETWORK = "derby-network"

        @JvmStatic
        val DERBY_INMEMORY = "derby-inmemory"

        private val randomDerbyPort: Int = findFreePort()

        @JvmStatic
        fun databaseName(project: Project): String {
            return PropertyUtil.resolveValue(project, "database", DERBY_INMEMORY).toString()
        }

        @JvmStatic
        fun dbConfigStream(project: Project): InputStream? {
            val dbname = databaseName(project)
            return {}::class.java.classLoader.getResourceAsStream("database-conf/deploy-repository.yaml.${dbname}")
        }

        @JvmStatic
        fun isDerby(project: Project): Boolean {
            val dbname = databaseName(project)
            return isDerby(dbname)
        }

        @JvmStatic
        fun isDerby(name: String): Boolean {
            return name == DERBY_NETWORK || name == DERBY_INMEMORY || name == DERBY
        }

        @JvmStatic
        fun isDerbyNetwork(project: Project): Boolean {
            val dbName = databaseName(project)
            return dbName == DERBY_NETWORK || dbName == DERBY
        }

        @JvmStatic
        fun assertNotDerby(project: Project, message: String) {
            val dbname = databaseName(project)
            if (isDerby(dbname)) {
                throw GradleException(message)
            }
        }

        @JvmStatic
        fun dbConfig(project: Project): TreeNode? {
            val from = dbConfigStream(project)
            return if (from != null) YamlFileUtil.readTree(from) else null
        }

        @JvmStatic
        private fun enrichDatabase(project: Project, database: Database): Database {
            database.derbyPort =
                if (project.hasProperty("derbyPort"))
                    Integer.valueOf(project.property("derbyPort").toString())
                else
                    randomDerbyPort

            database.logSql =
                if (project.hasProperty("logSql"))
                    project.property("logSql").toString().toBoolean()
                else
                    database.logSql

            return database
        }

        @JvmStatic
        fun getDatabase(project: Project): Database {
            val databases = project.extensions.getByType(DeployIntegrationServerExtension::class.java).databases
            val db = if (databases.isEmpty()) Database(databaseName(project)) else databases.first()
            return enrichDatabase(project, db)
        }

        @JvmStatic
        fun getResolveDbFilePath(project: Project): Path {
            val composeFileName = dockerComposeFileName(project)
            return DockerComposeUtil.getResolvedDockerPath(project, "database-compose/$composeFileName")
        }

        @JvmStatic
        fun dockerComposeFileName(project: Project): String {
            val dbName = databaseName(project)
            return "docker-compose_${dbName}.yaml"
        }

        private val postgresParams: DbParameters = DbParameters(
            "org.postgresql:postgresql",
            "org.postgresql.Driver",
            "org.dbunit.ext.postgresql.PostgresqlDataTypeFactory",
            null,
            "\"?\""
        )

        private val mysqlParams: DbParameters = DbParameters(
            "mysql:mysql-connector-java",
            "com.mysql.jdbc.Driver",
            "org.dbunit.ext.mysql.MySqlDataTypeFactory",
            "org.dbunit.ext.mysql.MySqlMetadataHandler",
            "`?`"
        )

        private val oracle19Params: DbParameters = DbParameters(
            "com.oracle.database.jdbc:ojdbc11",
            "oracle.jdbc.OracleDriver",
            "org.dbunit.ext.oracle.OracleDataTypeFactory",
            null,
            "\"?\""
        )

        private val mssqlParams: DbParameters = DbParameters(
            "com.microsoft.sqlserver:mssql-jdbc",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "org.dbunit.ext.mssql.MsSqlDataTypeFactory",
            null,
            "\"?\""
        )

        private val derbyParams: DbParameters = DbParameters(
            "org.apache.derby:derby",
            null,
            null,
            null,
            "\"?\""
        )

        private val derbyNetworkParams: DbParameters = DbParameters(
            "org.apache.derby:derbyclient",
            null,
            null,
            null,
            "\"?\""
        )


        @JvmStatic
        fun detectDbDependencies(db: String): DbParameters {
            return when (db) {
                DERBY -> derbyNetworkParams
                DERBY_INMEMORY -> derbyParams
                DERBY_NETWORK -> derbyNetworkParams
                MSSQL -> mssqlParams
                MYSQL, MYSQL8 -> mysqlParams
                ORACLE19 -> oracle19Params
                POSTGRES, POSTGRES12 -> postgresParams
                else -> derbyNetworkParams
            }

        }
    }
}
