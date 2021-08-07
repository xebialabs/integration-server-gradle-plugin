package com.xebialabs.gradle.integration.tasks.database

import com.xebialabs.gradle.integration.constant.PluginConstant
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractDbUnitDataDistTask
import com.xebialabs.gradle.integration.util.DbConfigurationUtil
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.PostgresDbUtil
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

    private def getConfiguration() {
        def dbConfig = DbUtil.dbConfig(project)
        def username = dbConfig.getString('xl.repository.database.db-username')
        def password = dbConfig.getString('xl.repository.database.db-password')
        def url = dbConfig.getString('xl.repository.database.db-url')
        return new Tuple3(username, password, url)
    }

    private def configureDataSet() {
        FlatXmlDataSetBuilder provider = new FlatXmlDataSetBuilder()
        provider.setColumnSensing(true)
        provider.setCaseSensitiveTableNames(true)
        def destinationDir = project.buildDir.toPath().resolve(PluginConstant.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        def dataFile = Paths.get("${destinationDir}/xld-is-data-${project.xldIsDataVersion}-repository/data.xml")
        return provider.build(new FileInputStream(dataFile.toFile()))
    }

    @TaskAction
    def runImport() {
        DbUtil.assertNotDerby(project, 'import job cannot be executed with Derby in network or in-memory configuration.')

        def dbname = DbUtil.databaseName(project)
        def dbDependency = DbUtil.detectDbDependencies(dbname)
        def dbConfig = getConfiguration()
        def properties = DbConfigurationUtil.connectionProperties(dbConfig.get(0), dbConfig.get(1))
        def driverConnection = DbConfigurationUtil.createDriverConnection(dbDependency.getDriverClass(), dbConfig.get(2), properties)
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
