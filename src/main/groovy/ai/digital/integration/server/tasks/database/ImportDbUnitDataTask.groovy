package ai.digital.integration.server.tasks.database

import ai.digital.integration.server.constant.PluginConstant
import ai.digital.integration.server.tasks.DownloadAndExtractDbUnitDataDistTask
import ai.digital.integration.server.util.DbConfigurationUtil
import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.PostgresDbUtil
import com.fasterxml.jackson.databind.node.TextNode
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class ImportDbUnitDataTask extends DefaultTask {
    static NAME = "importDbUnitData"

    ImportDbUnitDataTask() {
        this.configure {
            group = PluginConstant.PLUGIN_GROUP
            dependsOn(DownloadAndExtractDbUnitDataDistTask.NAME)
        }
    }

    private String getDbPropValue(String propName) {
        def dbConfig = DbUtil.dbConfig(project)
        ((TextNode) dbConfig.get('xl.repository').get("database").get(propName)).textValue()
    }

    private Tuple3<String, String, String> getConfiguration() {
        def username = getDbPropValue("db-username")
        def password = getDbPropValue("db-password")
        def url = getDbPropValue("db-url")
        new Tuple3<String, String, String>(username, password, url)
    }

    private def configureDataSet() {
        FlatXmlDataSetBuilder provider = new FlatXmlDataSetBuilder()
        provider.setColumnSensing(true)
        provider.setCaseSensitiveTableNames(true)
        def destinationDir = project.buildDir.toPath().resolve(PluginConstant.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        def dataFile = Paths.get("${destinationDir}/xld-is-data-${project.xldIsDataVersion}-repository/data.xml")
        provider.build(new FileInputStream(dataFile.toFile()))
    }

    @TaskAction
    def runImport() {
        DbUtil.assertNotDerby(project, 'import job cannot be executed with Derby in network or in-memory configuration.')

        def dbname = DbUtil.databaseName(project)
        def dbDependency = DbUtil.detectDbDependencies(dbname)
        def dbConfig = getConfiguration()
        def properties = DbConfigurationUtil.connectionProperties(dbConfig.getFirst(), dbConfig.getSecond())

        def driverConnection = DbConfigurationUtil.createDriverConnection(dbDependency.getDriverClass(), dbConfig.getThird(), properties)
        def connection = DbConfigurationUtil.configureConnection(driverConnection, dbDependency)
        try {
            def dataSet = configureDataSet()
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
            if (dbname == DbUtil.POSTGRES) {
                PostgresDbUtil.resetSequences(project.logger, driverConnection)
            }
        } finally {
            connection.close()
            driverConnection.close()
        }
    }
}
