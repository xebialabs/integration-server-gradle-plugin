package ai.digital.integration.server.common.domain

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class Database(objects: ObjectFactory) {

    var derbyPort: Property<Int> = objects.property<Int>().value(1527)

    var driverVersions: MapProperty<String, String> =
        objects.mapProperty(String::class.java, String::class.java).value(
            mutableMapOf(
            "mssql" to "8.4.1.jre8",
            "mysql" to "8.0.22",
            "mysql-8" to "8.0.22",
            "oracle-19c-se" to "21.1.0.0",
            "postgres-10" to "42.2.9",
            "postgres-12" to "42.2.23",
        ))

    var logSql: Property<Boolean> = objects.property<Boolean>().value(false)
}
