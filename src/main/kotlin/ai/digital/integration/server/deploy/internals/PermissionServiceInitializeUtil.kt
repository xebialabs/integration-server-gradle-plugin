package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.domain.Permission
import org.gradle.api.Project
import java.io.File

class PermissionServiceInitializeUtil {
    companion object {
        private fun createFolders(project: Project) {
            project.logger.lifecycle("Preparing Permission service config folder.")

            arrayOf("bin/config", "log").forEach { folderName ->
                val folderPath = "${PermissionServiceUtil.getPermissionServiceWorkingDir(project)}/${folderName}"
                val folder = File(folderPath)
                folder.mkdirs()
                project.logger.lifecycle("Folder $folderPath has been created.")
            }
        }

        private fun createConfFile(project: Project, server: Permission) {
            project.logger.lifecycle("Creating application.yaml file")

            val serverDir = PermissionServiceUtil.getPermissionServiceWorkingDir(project)
            val deployRepositoryYaml = File("$serverDir/bin/config/application.yaml")
            deployRepositoryYaml.createNewFile()

            DbUtil.permissionDbConfig(project)?.let { config ->
                YamlFileUtil.writeFileValue(deployRepositoryYaml, config)
            }

            val configuredTemplate = deployRepositoryYaml.readText(Charsets.UTF_8)
                    .replace("{{DB_PORT}}", DbUtil.getPort(project).toString())
            deployRepositoryYaml.writeText(configuredTemplate)
        }

        fun prepare(project: Project) {
            val server = PermissionServiceUtil.getPermissionService(project)
            project.logger.lifecycle("Preparing serve ${server.version} before launching it.")
            createFolders(project)
            createConfFile(project, server)
        }

        fun grantPermissionsToIntegrationServerFolder(project: Project) {
            if (PermissionServiceUtil.isDockerBased(project)) {
                val workDir = IntegrationServerUtil.getDist(project)

                File(workDir).walk().forEach {
                    FileUtil.grantRWPermissions(it)
                }
            }
        }

        fun waitForBoot(project: Project, process: Process?) {
            val server = PermissionServiceUtil.getPermissionService(project)
            val url = "http://localhost:${server.httpPort}/actuator"
            WaitForBootUtil.byPort(project, "Permission service", url, process, server.pingRetrySleepTime, server.pingTotalTries)
        }
    }
}
