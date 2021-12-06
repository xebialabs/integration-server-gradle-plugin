package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.DbConfigurationUtil
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PostgresDbUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractDbUnitDataDistTask
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.FileInputStream
import java.nio.file.Paths

open class ImportDbUnitDataTask : DefaultTask() {

    companion object {
        const val NAME = "importDbUnitData"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractDbUnitDataDistTask.NAME)
        this.onlyIf {
            !DbUtil.isDerby(project) && DeployExtensionUtil.getExtension(project).xldIsDataVersion != null
        }
    }

    private fun getConfiguration(): Triple<String, String, String> {
        val username = DbUtil.getDbPropValue(project, "db-username")
        val password = DbUtil.getDbPropValue(project, "db-password")
        val url = DbUtil.getDbPropValue(project, "db-url")

        return Triple(username, password, url)
    }

    private fun configureDataSet(): FlatXmlDataSet? {
        val provider = FlatXmlDataSetBuilder()
        provider.isColumnSensing = true
        provider.isCaseSensitiveTableNames = true
        val version = DeployExtensionUtil.getExtension(project).xldIsDataVersion
        val dataFile = Paths.get("${IntegrationServerUtil.getDist(project)}/xld-is-data-${version}-repository/data.xml")
        return provider.build(FileInputStream(dataFile.toFile()))
    }

    @TaskAction
    fun runImport() {
        val dbname = DbUtil.databaseName(project)
        val dbDependency = DbUtil.detectDbDependencies(dbname)
        val dbConfig = getConfiguration()
        val properties = DbConfigurationUtil.connectionProperties(dbConfig.first, dbConfig.second)

        val driverConnection =
            DbConfigurationUtil.createDriverConnection(dbDependency.driverClass.orEmpty(), dbConfig.third, properties)
        val connection = DbConfigurationUtil.configureConnection(driverConnection, dbDependency)
        try {
            val dataSet = configureDataSet()
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
            if (dbname == DbUtil.POSTGRES) {
                PostgresDbUtil.resetSequences(project, driverConnection)
            }
        } finally {
            connection.close()
            driverConnection.close()
        }
    }
}
