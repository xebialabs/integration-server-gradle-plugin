package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Database(objects: ObjectFactory) {

    var derbyPort: Int = objects.property<Int>().value(1527).get()

    var databasePort: Int = objects.property<Int>().value(1527).get()

    var driverVersions: Map<String, String> =
        objects.mapProperty(String::class.java, String::class.java).value(
            mutableMapOf(
                "mssql" to "8.4.1.jre8",
                "mysql" to "8.0.22",
                "mysql-8" to "8.0.22",
                "oracle-19c-se" to "21.1.0.0",
                "postgres-10" to "42.2.9",
                "postgres-12" to "42.2.23",
            )).get()

    var logSql: Boolean = objects.property<Boolean>().value(false).get()
}
