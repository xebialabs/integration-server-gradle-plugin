package ai.digital.integration.server.common.centralConfiguration

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.CentralConfigurationServer
import ai.digital.integration.server.common.util.CentralConfigurationServerUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EnvironmentUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

open class StartCentralConfigurationServerTask : DefaultTask() {
    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(DownloadAndExtractCentralConfigurationServerDistTask.NAME)
        this.dependsOn(PrepareCentralConfigurationServerTask.NAME)
        this.dependsOn(CentralConfigurationServerOverlaysTask.NAME)
        this.dependsOn(CentralConfigurationServerYamlPatchTask.NAME)

        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }
    }
    companion object {
        const val NAME = "startCentralConfigurationServer"
    }


    @TaskAction
    fun launch() {
        val centralConfigServer = CentralConfigurationServerUtil.getCentralConfigurationServer(project)
        val binDir = CentralConfigurationServerUtil.getBinDir(project, centralConfigServer)
        project.logger.lifecycle("Launching central configuration server from '${binDir}'.")

        val environment = EnvironmentUtil.getEnv(
                project,
                "JDK_JAVA_OPTIONS",
                centralConfigServer.debugSuspend,
                centralConfigServer.debugPort,
                CentralConfigurationServerUtil.logFileName()
        )

        project.logger.info("Starting central configuration server with environment: $environment")

        val process = ProcessUtil.exec(
                mapOf(
                        "command" to "run",
                        "environment" to environment,
                        "workDir" to binDir,
                        "discardIO" to centralConfigServer.stdoutFileName.isNullOrEmpty(),
                        "redirectTo" to (
                                if (!centralConfigServer.stdoutFileName.isNullOrEmpty())
                                    File(
                                            "${
                                                CentralConfigurationServerUtil.getLogDir(
                                                        project,
                                                        centralConfigServer
                                                )
                                            }/${centralConfigServer.stdoutFileName}"
                                    )
                                else null)
                )
        )
        project.logger.lifecycle(
                "Central Configuration Server '${centralConfigServer.version}' successfully started on PID [${process.pid()}] with command [${
                    process.info().commandLine().orElse("")
                }] on port ${CentralConfigurationServerUtil.readDeployitConfProperty(project, "http.port")}"
        )
        waitForBoot(project, process, centralConfigServer)
    }

    fun waitForBoot(project: Project, process: Process, centralConfigurationServer: CentralConfigurationServer) {
        val server = DeployServerUtil.getServer(project)
        val ccLog =
                project.file("${CentralConfigurationServerUtil.getServerPath(project, centralConfigurationServer)}/log/${CentralConfigurationServerUtil.logFileName()}.log")
        WaitForBootUtil.byLog(project,
                "Central Config Server ${centralConfigurationServer.version}",
                ccLog,
                "Started ConfigServerApplication.",
                process,
                server.pingRetrySleepTime,
                server.pingTotalTries)
    }
}
