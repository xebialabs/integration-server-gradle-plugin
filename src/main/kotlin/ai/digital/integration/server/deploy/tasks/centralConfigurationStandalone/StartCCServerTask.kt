package ai.digital.integration.server.deploy.tasks.centralConfigurationStandalone

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.deploy.domain.CentralConfigurationStandalone
import ai.digital.integration.server.deploy.internals.CentralConfigurationStandaloneUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EnvironmentUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

open class StartCCServerTask : DefaultTask() {
    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(DownloadAndExtractCCDistTask.NAME)
        this.dependsOn(PrepareCCTask.NAME)
        this.dependsOn(CentralConfigOverlaysTask.NAME)

        this.onlyIf {
            CentralConfigurationStandaloneUtil.hasCC(project)
        }
    }
    companion object {
        const val NAME = "startCentralConfigServer"
    }


    @TaskAction
    fun launch() {
        val centralConfigServer = CentralConfigurationStandaloneUtil.getCC(project)
        if(!CentralConfigurationStandaloneUtil.isDockerBased(project)) {
            val binDir = CentralConfigurationStandaloneUtil.getBinDir(project, centralConfigServer)
            project.logger.lifecycle("Launching Central Config Server from '${binDir}'.")

            val environment = EnvironmentUtil.getEnv(
                project,
                "JDK_JAVA_OPTIONS",
                centralConfigServer.debugSuspend,
                centralConfigServer.debugPort,
                logFileName()
            )

            project.logger.info("Starting Central Config Server with environment: $environment")

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
                                        CentralConfigurationStandaloneUtil.getLogDir(
                                            project,
                                            centralConfigServer
                                        )
                                    }/${centralConfigServer.stdoutFileName}"
                                )
                            else null)
                )
            )
            project.logger.lifecycle(
                "Central Config Server '${centralConfigServer.version}' successfully started on PID [${process.pid()}] with command [${
                    process.info().commandLine().orElse("")
                }]."
            )
            waitForBoot(project, process, centralConfigServer)
        } else {
            CentralConfigurationStandaloneUtil.createNetwork(project)
            CentralConfigurationStandaloneUtil.runDockerBasedInstance(project, centralConfigServer)
        }
    }

    fun waitForBoot(project: Project, process: Process, CentralConfigurationStandalone: CentralConfigurationStandalone) {
        val server = DeployServerUtil.getServer(project)
        val ccLog =
                project.file("${CentralConfigurationStandaloneUtil.getCCServerPath(project, CentralConfigurationStandalone)}/log/${logFileName()}.log")
        WaitForBootUtil.byLog(project,
                "Central Config Server ${CentralConfigurationStandalone.version}",
                ccLog,
                "Started ConfigServerApplication.",
                process,
                server.pingRetrySleepTime,
                server.pingTotalTries)
    }

    private fun logFileName(): String {
        return "central-config"
    }
}
