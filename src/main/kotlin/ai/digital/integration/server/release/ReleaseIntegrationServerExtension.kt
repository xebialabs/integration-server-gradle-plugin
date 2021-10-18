package ai.digital.integration.server.release

import ai.digital.integration.server.common.domain.Server
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer

open class ReleaseIntegrationServerExtension(
    val servers: NamedDomainObjectContainer<Server>,
) {

    fun servers(closure: Closure<NamedDomainObjectContainer<Server>>) {
        servers.configure(closure)
    }
}
