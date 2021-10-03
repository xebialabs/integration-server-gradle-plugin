package ai.digital.integration.server.util

import ai.digital.integration.server.domain.DbParameters
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import java.sql.Connection
import java.sql.Driver
import java.util.*

class DbConfigurationUtil {

    companion object {
        @JvmStatic
        fun connectionProperties(username: String, password: String): Properties {
            val properties = Properties()
            properties["user"] = username
            properties["password"] = password
            return properties
        }

        @JvmStatic
        fun createDriverConnection(driverClass: String, url: String, properties: Properties): Connection {
            val driver = Class.forName(driverClass).getDeclaredConstructor().newInstance() as Driver
            val driverConnection = driver.connect(url, properties)
            driverConnection.autoCommit = true
            return driverConnection
        }

        @JvmStatic
        fun configureConnection(driverConnection: Connection, dbDependency: DbParameters): DatabaseConnection {
            val connection = DatabaseConnection(driverConnection, "public")
            val config = connection.getConfig()
            config.setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, true)
            config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true)
            config.setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, dbDependency.escapePattern)

            val dataTypeFactory = Class.forName(dbDependency.dataFactory).getDeclaredConstructor().newInstance()
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory)

            val metaFactory = dbDependency.metaFactory
            if (metaFactory != null && !metaFactory.isEmpty()) {
                val metadataHandler = Class.forName(metaFactory).getDeclaredConstructor().newInstance()
                config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, metadataHandler)
            }

            return connection
        }
    }
}
