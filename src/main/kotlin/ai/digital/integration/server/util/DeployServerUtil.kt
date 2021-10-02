package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.Server
import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.Paths

class DeployServerUtil {

    companion object {
        @JvmStatic
        fun getServer(project: Project): Server {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
            val server = ext.servers.first()
            server.debugPort = getDebugPort(project, server)
            server.httpPort = getHttpPort(project, server)
            server.version = getServerVersion(project, server)

            if (!server.dockerImage.isNullOrEmpty()) {
                server.runtimeDirectory = null
            }

            return server
        }

        @JvmStatic
        fun getServerWorkingDir(project: Project): String {
            val server = getServer(project)

            return when {
                isDockerBased(project) -> {
                    val workDir = getRelativePathInIntegrationServerDist(project, "deploy")
                    workDir.toAbsolutePath().toString()
                }
                server.runtimeDirectory == null -> {
                    val targetDir = getServerDistFolderPath(project).toString()
                    Paths.get(targetDir, "xl-deploy-${server.version}-server").toAbsolutePath().toString()
                }
                else -> {
                    val target = project.projectDir.toString()
                    Paths.get(target, server.runtimeDirectory).toAbsolutePath().toString()
                }
            }
        }

        @JvmStatic
        fun getRelativePathInIntegrationServerDist(project: Project, relativePath: String): Path {
            return Paths.get("${IntegrationServerUtil.getDist(project)}/${relativePath}")
        }

        @JvmStatic
        fun getServerDistFolderPath(project: Project): Path {
            return Paths.get(IntegrationServerUtil.getDist(project))
        }

        @JvmStatic
        fun isDockerBased(project: Project): Boolean {
            return !getServer(project).dockerImage.isNullOrBlank()
        }

        @JvmStatic
        private fun getServerVersion(project: Project, server: Server): String? {
            return if (project.hasProperty("xlDeployVersion"))
                project.property("xlDeployVersion").toString()
            else
                server.version
        }

        @JvmStatic
        private fun getHttpPort(project: Project, server: Server): Int {
            return if (project.hasProperty("serverHttpPort"))
                Integer.valueOf(project.property("serverHttpPort").toString())
            else
                server.httpPort
        }

        @JvmStatic
        private fun getDebugPort(project: Project, server: Server): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "serverDebugPort", server.debugPort)
            } else {
                null
            }
        }
    }
}
