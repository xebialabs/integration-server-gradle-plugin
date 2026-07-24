package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.DbConfigurationUtil
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PostgresDbUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractDbUnitDataDistTask
import ai.digital.integration.server.deploy.tasks.server.StartDeployServerInstanceTask
import org.dbunit.database.DatabaseConfig
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
            DeployExtensionUtil.getExtension(project).xldIsDataVersion != null
        }
    }

    // True when the consumer overrides the DBUnit coordinate (e.g. FE's xld-ci-explorer-data). The backend
    // default (xld-is-data) returns false and keeps the exact legacy import behavior — unaffected by FE fixes.
    private fun isCustomDataArtifact(): Boolean =
        DownloadAndExtractDbUnitDataDistTask.isCustomDataArtifact(
            DeployExtensionUtil.getExtension(project).xldIsDataArtifact)

    private fun getConfiguration(): Triple<String, String, String> {
        val username = DbUtil.getDbPropValue(project, "db-username")
        val password = DbUtil.getDbPropValue(project, "db-password")
        val url = DbUtil.getDbPropValue(project, "db-url")
        val updatedDbUrl = url.replace("{{DB_PORT}}", DbUtil.getPort(project).toString())
        return Triple(username, password, updatedDbUrl)
    }

    private fun configureDataSet(): FlatXmlDataSet? {
        val provider = FlatXmlDataSetBuilder()
        provider.isColumnSensing = true
        provider.isCaseSensitiveTableNames = true
        val extension = DeployExtensionUtil.getExtension(project)
        val version = extension.xldIsDataVersion
        // Derive the extracted repository folder from the SAME helper DownloadAndExtractDbUnitDataDistTask
        // extracts into, so the read path can never drift from the write path.
        val repoFolder = DownloadAndExtractDbUnitDataDistTask.repositoryFolderName(extension.xldIsDataArtifact, version.toString())
        val dataFile = Paths.get("${IntegrationServerUtil.getDist(project)}/${repoFolder}/data.xml")
        project.logger.lifecycle("[DbUnit][import] Loading dataset from artifact '${extension.xldIsDataArtifact}:${version}' -> ${dataFile}")
        return if (isCustomDataArtifact()) {
            // FE (custom artifact): build from the File so DBUnit sets the base URI to data.xml's location, letting
            // the flat-XML DOCTYPE ("xl-deploy-repository-dump.dtd") resolve from the same -repository folder (we
            // ship the .dtd alongside). A raw FileInputStream would look up the DTD relative to the process working
            // dir and fail with FileNotFoundException.
            provider.build(dataFile.toFile())
        } else {
            // Legacy backend (xld-is-data) — unchanged stream-based build.
            provider.build(FileInputStream(dataFile.toFile()))
        }
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
        if (isCustomDataArtifact()) {
            // FE (custom artifact) datasets contain empty-string column values (e.g.
            // XLD_ACTIVE_TASKS_METADATA.metadata_value); allow them instead of failing the CLEAN_INSERT with
            // "value is empty but must contain a value". Backend xld-is-data keeps the default (unset) behavior.
            connection.config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true)
        }
        try {
            val dataSet = configureDataSet()
            project.logger.lifecycle("[DbUnit][import] Executing CLEAN_INSERT into '${dbname}' (${dbConfig.third})")
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
            project.logger.lifecycle("[DbUnit][import] CLEAN_INSERT completed for '${dbname}'")
            if (dbname == DbUtil.POSTGRES) {
                project.logger.lifecycle("[DbUnit][import] Resetting Postgres sequences")
                PostgresDbUtil.resetSequences(project, driverConnection)
            }
        } finally {
            connection.close()
            driverConnection.close()
        }
    }
}
