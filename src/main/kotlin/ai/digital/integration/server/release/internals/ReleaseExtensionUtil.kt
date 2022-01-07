package ai.digital.integration.server.release.internals

import ai.digital.integration.server.common.domain.DevOpsAsCode
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.release.ReleaseIntegrationServerExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project


class ReleaseExtensionUtil {

    companion object {
        private const val RELEASE_IS_EXTENSION_NAME: String = "releaseIntegrationServer"

        fun getExtension(project: Project): ReleaseIntegrationServerExtension {
            return project.extensions.getByType(ReleaseIntegrationServerExtension::class.java)
        }

        fun createReleaseExtension(project: Project) {
            val servers: NamedDomainObjectContainer<Server> = project.container(Server::class.java)

            servers.forEach { server ->
                server.devOpsAsCodes = project.container(DevOpsAsCode::class.java)
            }

            project.extensions.create(
                RELEASE_IS_EXTENSION_NAME,
                ReleaseIntegrationServerExtension::class.java,
                servers
            )
        }
    }
}
