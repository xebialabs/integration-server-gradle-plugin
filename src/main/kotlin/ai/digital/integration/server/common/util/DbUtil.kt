package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Database
import ai.digital.integration.server.common.domain.DbParameters
import ai.digital.integration.server.common.util.HTTPUtil.Companion.findFreePort
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.node.TextNode
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.InputStream
import java.nio.file.Path

class DbUtil {

    companion object {
        const val POSTGRES = "postgres-10"
        const val POSTGRES12 = "postgres-12"
        const val ORACLE19 = "oracle-19c-se"
        const val MYSQL = "mysql"
        const val MYSQL8 = "mysql-8"
        const val MSSQL = "mssql"
        const val DERBY = "derby"
        const val DERBY_NETWORK = "derby-network"
        const val DERBY_INMEMORY = "derby-inmemory"

        private val randomDatabasePort: Int = findFreePort()

        fun databaseName(project: Project): String {
            return PropertyUtil.resolveValue(project, "database", DERBY_INMEMORY).toString()
        }

        private fun dbConfigStream(project: Project): InputStream? {
            val dbname = databaseName(project)
            return {}::class.java.classLoader.getResourceAsStream("database-conf/deploy-repository.yaml.${dbname}")
        }

        fun isDerby(project: Project): Boolean {
            val dbname = databaseName(project)
            return isDerby(dbname)
        }

        fun isDerby(name: String): Boolean {
            return name == DERBY_NETWORK || name == DERBY_INMEMORY || name == DERBY
        }

        fun isDerbyNetwork(project: Project): Boolean {
            val dbName = databaseName(project)
            return dbName == DERBY_NETWORK || dbName == DERBY
        }

        fun assertNotDerby(project: Project, message: String) {
            val dbname = databaseName(project)
            if (isDerby(dbname)) {
                throw GradleException(message)
            }
        }

        fun dbConfig(project: Project): TreeNode? {
            val from = dbConfigStream(project)
            return if (from != null) YamlFileUtil.readTree(from) else null
        }

        fun getDbPropValue(project: Project, propName: String): String {
            val dbConfig = dbConfig(project)

            dbConfig?.let { config ->
                return (config.get("xl.repository").get("database").get(propName) as TextNode).textValue()
            }

            return ""
        }

        private fun enrichDatabase(project: Project, database: Database): Database {
            database.databasePort =
                if (project.hasProperty("databasePort"))
                    Integer.valueOf(project.property("databasePort").toString())
                else
                    randomDatabasePort

            database.logSql =
                if (project.hasProperty("logSql"))
                    project.property("logSql").toString().toBoolean()
                else
                    database.logSql

            return database
        }

        fun getPort(project: Project): Int {
            return getDatabase(project).databasePort ?: randomDatabasePort
        }

        fun getDatabase(project: Project): Database {
            val database = DeployExtensionUtil.getExtension(project).database.get()
            return enrichDatabase(project, database)
        }

        fun getResolveDbFilePath(project: Project): Path {
            val composeFileName = dockerComposeFileName(project)
            return DockerComposeUtil.getResolvedDockerPath(project, "database-compose/$composeFileName")
        }

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
            "com.mysql:mysql-connector-j",
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

        fun getResolvedDBDockerComposeFile(resultComposeFilePath: Path, project: Project) {
            val serverTemplate = resultComposeFilePath.toFile()
            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("{{DB_PORT}}", getPort(project).toString())
            serverTemplate.writeText(configuredTemplate)
        }
    }
}
