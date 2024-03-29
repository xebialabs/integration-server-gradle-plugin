package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Database(objects: ObjectFactory) {

    var databasePort: Int? = null

    var driverVersions: Map<String, String> =
        objects.mapProperty(String::class.java, String::class.java).value(
            mutableMapOf(
                "mssql" to "11.2.3.jre17",
                "mysql" to "8.1.0",
                "mysql-8" to "8.1.0",
                "oracle-19c-se" to "21.1.0.0",
                "postgres-10" to "42.6.0",
                "postgres-12" to "42.6.0"
            )).get()

    var logSql: Boolean = objects.property<Boolean>().value(false).get()
}
