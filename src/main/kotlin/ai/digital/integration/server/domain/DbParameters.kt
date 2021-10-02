package ai.digital.integration.server.domain

data class DbParameters(
    val driverDependency: String,
    val driverClass: String?,
    val dataFactory: String?,
    val metaFactory: String?,
    val escapePattern: String?
)
