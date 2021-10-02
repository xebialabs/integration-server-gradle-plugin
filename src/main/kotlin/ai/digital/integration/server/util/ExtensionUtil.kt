package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project


class ExtensionUtil {

    companion object {
        @JvmStatic
        val IS_EXTENSION_NAME: String = "integrationServer"

        @JvmStatic
        fun getExtension(project: Project): IntegrationServerExtension {
            return project.extensions.getByType(IntegrationServerExtension::class.java)
        }

        @JvmStatic
        fun createExtension(project: Project) {
            val servers: NamedDomainObjectContainer<Server> = project.container(Server::class.java)

            servers.forEach { server ->
                server.devOpsAsCodes = project.container(DevOpsAsCode::class.java)
            }

            project.extensions.create(
                IS_EXTENSION_NAME,
                IntegrationServerExtension::class.java,
                project.container(Cli::class.java),
                project.container(Database::class.java),
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
