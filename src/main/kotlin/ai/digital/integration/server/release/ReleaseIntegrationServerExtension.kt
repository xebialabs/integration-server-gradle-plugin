package ai.digital.integration.server.release

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.extension.CommonIntegrationServerExtension
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class ReleaseIntegrationServerExtension(
    project: Project,
    val servers: NamedDomainObjectContainer<Server>
) : CommonIntegrationServerExtension(project) {

    fun servers(closure: Closure<NamedDomainObjectContainer<Server>>) {
        servers.configure(closure)
    }
}
