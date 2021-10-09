package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.DriverDependencyAware

data class DbParameters(
    override val driverDependency: String,
    val driverClass: String?,
    val dataFactory: String?,
    val metaFactory: String?,
    val escapePattern: String?,
) : DriverDependencyAware(driverDependency)
