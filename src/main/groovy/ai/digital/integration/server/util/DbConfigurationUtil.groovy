package ai.digital.integration.server.util

import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IMetadataHandler
import org.dbunit.dataset.datatype.IDataTypeFactory

import java.sql.Connection
import java.sql.Driver

class DbConfigurationUtil {
    static def connectionProperties(username, password) {
        Properties properties = new Properties()
        properties.put('user', username)
        properties.put('password', password)
        properties
    }

    static Connection createDriverConnection(String driverClass, String url, Properties properties) {
        Driver driver = (Driver) Class.forName(driverClass).newInstance()
        Connection driverConnection = driver.connect(url, properties)
        driverConnection.setAutoCommit(true)
        driverConnection
    }

    static def configureConnection(driverConnection, dbDependency) {
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

        connection
    }
}
