package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.DriverDependencyAware

data class MqParameters(
    override val driverDependency: String,
    val driverClass: String,
    val url: String?,
    val userName: String,
    val password: String,
) : DriverDependencyAware(driverDependency)
