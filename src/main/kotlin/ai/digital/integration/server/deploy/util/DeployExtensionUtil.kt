package ai.digital.integration.server.deploy.util

import ai.digital.integration.server.common.domain.DevOpsAsCode
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.Test
import ai.digital.integration.server.deploy.DeployIntegrationServerExtension
import ai.digital.integration.server.deploy.domain.Cli
import ai.digital.integration.server.deploy.domain.Satellite
import ai.digital.integration.server.deploy.domain.Worker
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project


class DeployExtensionUtil {

    companion object {
        const val DEPLOY_IS_EXTENSION_NAME: String = "deployIntegrationServer"

        @JvmStatic
        fun getExtension(project: Project): DeployIntegrationServerExtension {
            return project.extensions.getByType(DeployIntegrationServerExtension::class.java)
        }

        @JvmStatic
        fun createDeployExtension(project: Project) {
            val servers: NamedDomainObjectContainer<Server> = project.container(Server::class.java)

            servers.forEach { server ->
                server.devOpsAsCodes = project.container(DevOpsAsCode::class.java)
            }

            project.extensions.create(
                DEPLOY_IS_EXTENSION_NAME,
                DeployIntegrationServerExtension::class.java,
                project,
                project.container(Cli::class.java),
                project.container(Satellite::class.java),
                servers,
                project.container(Test::class.java),
                project.container(Worker::class.java)
            )
        }

        @JvmStatic
        fun initialize(project: Project) {
            val extension = getExtension(project)
            extension.xldIsDataVersion = getXldIsDataVersion(project)
            extension.mqDriverVersions = getMqDriverVersions(project)
        }

        @JvmStatic
        private fun getXldIsDataVersion(project: Project): String? {
            return if (project.hasProperty("xldIsDataVersion"))
                project.property("xldIsDataVersion").toString()
            else
                null
        }

        @JvmStatic
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
