package ai.digital.integration.server.domain

open class Database(val name: String) {
    var derbyPort: Int? = null
    var driverVersions: Map<String, String> = mutableMapOf(
        "mssql" to "8.4.1.jre8",
        "mysql" to "8.0.22",
        "mysql-8" to "8.0.22",
        "oracle-19c-se" to "21.1.0.0",
        "postgres-10" to "42.2.9",
        "postgres-12" to "42.2.23",
    )

    var logSql: Boolean = false
}
