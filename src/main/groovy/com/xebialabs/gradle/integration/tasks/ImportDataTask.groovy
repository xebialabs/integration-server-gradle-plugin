package com.xebialabs.gradle.integration.tasks

import com.typesafe.config.ConfigFactory
import com.xebialabs.gradle.integration.util.DbUtil
import org.apache.commons.io.IOUtils
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

import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.Driver

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ImportDataTask extends DefaultTask {
    static NAME = "importData"

    ImportDataTask() {
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(DownloadAndExtractDataDistTask.NAME)
        }
    }

    private def getConfiguration() {
        def from = DbUtil.dbConfigFile(project)
        def configFileStr = IOUtils.toString(from, StandardCharsets.UTF_8.name())
        def dbConfig = ConfigFactory.parseString(configFileStr)
        def username = dbConfig.getString("xl.repository.database.db-username")
        def password = dbConfig.getString("xl.repository.database.db-password")
        def url = dbConfig.getString("xl.repository.database.db-url")
        return new Tuple3(username, password, url)
    }

    private def runImport() {
        def dbname = DbUtil.databaseName(project)
        if (DbUtil.isDerby(dbname)) {
            throw new GradleException('import job cannot be executed with Derby in network or in-memory configuration.')
        }
        def dbDependency = DbUtil.detectDbDependency(dbname)
        def dbConfig = getConfiguration()
        Properties properties = new Properties()
        properties.put("user", dbConfig.get(0))
        properties.put("password", dbConfig.get(1))

        Driver driver = (Driver) Class.forName(dbDependency.getDriverClass()).newInstance()
        Connection driverConnection = driver.connect(dbConfig.get(2), properties)
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
        def dataFile = "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/xld-is-data-${project.xldIsDataVersion}-repository/data.xml"
        IDataSet dataSet = provider.build(new FileInputStream(dataFile))
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet)
    }

    @TaskAction
    def importData() {
        runImport()
    }
}
