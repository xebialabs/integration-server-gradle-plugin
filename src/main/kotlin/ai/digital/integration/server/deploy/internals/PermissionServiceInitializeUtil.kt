package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.domain.Permission
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

class PermissionServiceInitializeUtil {
    companion object {
        private fun createFolders(project: Project) {
            project.logger.lifecycle("Preparing Permission service config folder.")

            arrayOf("config", "log").forEach { folderName ->
                val folderPath = "${PermissionServiceUtil.getPermissionServiceWorkingDir(project)}/${folderName}"
                val folder = File(folderPath)
                folder.mkdirs()
                project.logger.lifecycle("Folder $folderPath has been created.")
            }
        }

        private fun createConfFile(project: Project, server: Permission) {
            project.logger.lifecycle("Creating application.properties file")

            val file = project.file("${PermissionServiceUtil.getPermissionServiceWorkingDir(project)}/config/application.properties")
            file.createNewFile()

            file.writeText("xl.permission-service.database.db-driver-classname=org.postgresql.Driver\n")
            file.appendText("xl.permission-service.database.db-url=jdbc:postgresql://localhost:5431/postgres\n")
            file.appendText("xl.permission-service.database.db-password=postgres\n")
            file.appendText("xl.permission-service.database.db-username=postgres\n")
        }

        fun prepare(project: Project) {
            val server = PermissionServiceUtil.getPermissionService(project)
            project.logger.lifecycle("Preparing serve ${server.version} before launching it.")
            createFolders(project)
            createConfFile(project, server)
        }

        fun startServerFromClasspath(project: Project): Process {
            project.logger.lifecycle("startServerFromClasspath.")
            val server = DeployServerUtil.getServer(project)

            val classpath = project.configurations.getByName(DeployConfigurationsUtil.PERMISSION_SERVICE_DIST)
                    .filter { !it.name.endsWith("-sources.jar") }
                    .asPath
            project.logger.lifecycle("Launching Permission Server from classpath ${classpath}.")

            val jvmArgs = mutableListOf<String>()
            jvmArgs.addAll(server.jvmArgs)
            server.debugPort?.let {
                jvmArgs.addAll(JavaUtil.debugJvmArg(project, it, server.debugSuspend))
            }
            project.logger.lifecycle("I'm HEREEEEEE")
            val config = mutableMapOf(
                    "classpath" to classpath,
                    "discardIO" to (server.stdoutFileName == null),
                    "jvmArgs" to jvmArgs,
                    "mainClass" to "ai.digital.deploy.PermissionsServiceApplication",
                    "workDir" to File(PermissionServiceUtil.getPermissionServiceWorkingDir(project)),
            )

            server.stdoutFileName?.let {
                config["redirectTo"] = File("${DeployServerUtil.getLogDir(project, server)}/${it}")
            }

            project.properties["integrationServerJVMPath"]?.let {
                config.putAll(JavaUtil.jvmPath(project, it as String))
            }

            project.logger.lifecycle("Starting integration test server on port ${server.httpPort} from runtime dir ${server.runtimeDirectory}")

            val process = JavaUtil.execJava(config)

            project.logger.lifecycle("Launched server on PID [${process.pid()}] with command [${
                process.info().commandLine().orElse("")
            }].")

            return process
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
            val url = "http://localhost:4519/deployit/metadata/type"
//            val server = PermissionServiceUtil.getPermissionService(project)
            WaitForBootUtil.byPort(project, "Permission", url, process, 10, 10)
        }

        fun getDockerImageVersion(project: Project): String {
            val server = DeployServerUtil.getServer(project)
            return "${server.dockerImage}:${server.version}"
        }

        fun getDockerServiceName(project: Project): String {
            val server = DeployServerUtil.getServer(project)
            return "deploy-${server.version}"
        }

//        fun getResolvedDockerFile(project: Project): Path {
//            val server = DeployServerUtil.getServer(project)
//            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, DeployServerUtil.dockerServerRelativePath)
//
//            val serverTemplate = resultComposeFilePath.toFile()
//
//            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
//                    .replace("{{DEPLOY_SERVER_HTTP_PORT}}", server.httpPort.toString())
//                    .replace("{{DEPLOY_IMAGE_VERSION}}", getDockerImageVersion(project))
//                    .replace(
//                            "{{DEPLOY_PLUGINS_TO_EXCLUDE}}",
//                            server.defaultOfficialPluginsToExclude.joinToString(separator = ",")
//                    )
//                    .replace("{{DEPLOY_VERSION}}", server.version.toString())
//            serverTemplate.writeText(configuredTemplate)
//
//            return resultComposeFilePath
//        }
    }
}
