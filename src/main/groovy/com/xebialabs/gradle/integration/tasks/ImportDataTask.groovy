package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IMetadataHandler
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.datatype.IDataTypeFactory
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.sql.Connection
import java.sql.Driver

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP
import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class ImportDataTask extends DefaultTask {
    static NAME = "importData"

    ImportDataTask() {
        this.configure {
            group = PLUGIN_GROUP
//            dependsOn(DownloadAndExtractDataDistTask.NAME, StartIntegrationServerTask.NAME)
            dependsOn(DownloadAndExtractDataDistTask.NAME)
        }
    }

    private def runImport() {
        def dbname = DbUtil.databaseName(project)
        if (dbname == 'derby-network' || dbname == 'derby-inmemory') {
            throw new GradleException('import job cannot be executed with Derby in network or in-memory configuration.')
        }
        def dbDependency = DbUtil.detectDbDependency(dbname)

        Properties properties = new Properties()
        properties.put("user", "postgres")
        properties.put("password", "demo")

        Driver driver = (Driver) Class.forName(dbDependency.getDriverClass()).newInstance()
        Connection driverConnection = driver.connect("jdbc:postgresql://localhost:5432/xldrepo", properties)
        driverConnection.setAutoCommit(true)

        DatabaseConnection connection = new DatabaseConnection(driverConnection, "public")
        DatabaseConfig config = connection.getConfig()
        config.setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, true)
        config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true)
        config.setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, dbDependency.getEscapePattern())

        IDataTypeFactory dataTypeFactory = (IDataTypeFactory) Class.forName(dbDependency.getDataFactory()).newInstance()
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory)

        String metaFactory = dbDependency.getMetaFactory()
        if (metaFactory != null && !metaFactory.isEmpty()) {
            IMetadataHandler metadataHandler = (IMetadataHandler) Class.forName(metaFactory).newInstance()
            config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, metadataHandler)
        }

        FlatXmlDataSetBuilder provider = new FlatXmlDataSetBuilder()
        provider.setColumnSensing(true)
        provider.setCaseSensitiveTableNames(true)
//        IDataSet dataSet = provider.build(new FileInputStream("${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/xld-ci-explorer-${project.xldCiExplorerDataVersion}-repository/data.xml"))
        IDataSet dataSet = provider.build(new FileInputStream("${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/xld-ci-explorer-${'9.6.1-SNAPSHOT'}-repository/data.xml"))
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
    }

    @TaskAction
    def importData() {
        runImport()
    }
}
