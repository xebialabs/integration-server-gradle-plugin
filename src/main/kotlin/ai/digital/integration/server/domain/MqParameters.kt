package ai.digital.integration.server.domain

data class MqParameters(
    val driverDependency: String,
    val driverClass: String,
    val url: String?,
    val userName: String,
    val password: String
)
