package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.*
import ai.digital.integration.server.deploy.DeployIntegrationServerExtension
import ai.digital.integration.server.deploy.domain.Satellite
import ai.digital.integration.server.deploy.domain.Worker
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project


class DeployExtensionUtil {

    companion object {
        const val DEPLOY_IS_EXTENSION_NAME: String = "deployIntegrationServer"

        fun getExtension(project: Project): DeployIntegrationServerExtension {
            return project.extensions.getByType(DeployIntegrationServerExtension::class.java)
        }

        fun createDeployExtension(project: Project) {
            val servers: NamedDomainObjectContainer<Server> = project.container(Server::class.java)

            servers.forEach { server ->
                server.devOpsAsCodes = project.container(DevOpsAsCode::class.java)
            }

            project.extensions.create(
                DEPLOY_IS_EXTENSION_NAME,
                DeployIntegrationServerExtension::class.java,
                project,
                project.container(Satellite::class.java),
                servers,
                project.container(Test::class.java),
                project.container(Worker::class.java),
                project.container(Infrastructure::class.java)
            )
        }

        fun initialize(project: Project) {
            val extension = getExtension(project)
            extension.xldIsDataVersion = getXldIsDataVersion(project)
            extension.mqDriverVersions = getMqDriverVersions(project)
        }

        private fun getXldIsDataVersion(project: Project): String? {
            return if (project.hasProperty("xldIsDataVersion"))
                project.property("xldIsDataVersion").toString()
            else
                null
        }

        @Suppress("UNCHECKED_CAST")
        private fun getMqDriverVersions(project: Project): MutableMap<String, String> {
            return if (project.hasProperty("mqDriverVersions"))
                project.property("mqDriverVersions") as MutableMap<String, String>
            else
                mutableMapOf(
                    "activemq" to "5.16.2",
                    "rabbitmq" to "2.2.0"
                )
        }
    }
}
