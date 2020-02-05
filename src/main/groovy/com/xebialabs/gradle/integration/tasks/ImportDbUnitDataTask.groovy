package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IMetadataHandler
import org.dbunit.dataset.datatype.IDataTypeFactory
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.sql.Connection
import java.sql.Driver

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ImportDbUnitDataTask extends DefaultTask {
    static NAME = "importDbUnitData"

    ImportDbUnitDataTask() {
        this.configure {
            group = PLUGIN_GROUP
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

    private def connectionProperties(username, password) {
        Properties properties = new Properties()
        properties.put('user', username)
        properties.put('password', password)
        return properties
    }

    private def createDriverConnection(driverClass, url, properties) {
        Driver driver = (Driver) Class.forName(driverClass).newInstance()
        Connection driverConnection = driver.connect(url, properties)
        driverConnection.setAutoCommit(true)
        return driverConnection
    }

    private def configureConnection(driverConnection, dbDependency) {
        DatabaseConnection connection = new DatabaseConnection(driverConnection, 'public')
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

        return connection
    }

    private def configureDataSet() {
        FlatXmlDataSetBuilder provider = new FlatXmlDataSetBuilder()
        provider.setColumnSensing(true)
        provider.setCaseSensitiveTableNames(true)
        def destinationDir = project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()
        def dataFile = Paths.get("${destinationDir}/xld-is-data-${project.xldIsDataVersion}-repository/data.xml")
        return provider.build(new FileInputStream(dataFile.toFile()))
    }

    @TaskAction
    def runImport() {
        DbUtil.assertNotDerby(project, 'import job cannot be executed with Derby in network or in-memory configuration.')

        def dbname = DbUtil.databaseName(project)
        def dbDependency = DbUtil.detectDbDependency(dbname)
        def dbConfig = getConfiguration()
        def properties = connectionProperties(dbConfig.get(0), dbConfig.get(1))
        def driverConnection = createDriverConnection(dbDependency.getDriverClass(), dbConfig.get(2), properties)
        def connection = configureConnection(driverConnection, dbDependency)
        def dataSet = configureDataSet()
        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
        } finally {
            connection.close()
            driverConnection.close()
        }
    }
}
