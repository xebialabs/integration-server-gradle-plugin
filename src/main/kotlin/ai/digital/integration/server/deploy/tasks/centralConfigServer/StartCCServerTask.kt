package ai.digital.integration.server.deploy.tasks.centralConfigServer

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.deploy.domain.CentralConfigServer
import ai.digital.integration.server.deploy.internals.CentralConfigServerUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EnvironmentUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class StartCCServerTask : DefaultTask() {
    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(PrepareCCTask.NAME)
        this.dependsOn(DownloadAndExtractCCDistTask.NAME)
        this.dependsOn(CentralConfigOverlaysTask.NAME)
    }
    companion object {
        const val NAME = "startCentralConfigServer"
    }

    @TaskAction
    fun launch() {
        val centralConfigServer = CentralConfigServerUtil.getCC(project)
        val binDir = CentralConfigServerUtil.getBinDir(project, centralConfigServer)
        project.logger.lifecycle("Launching Central Config Server from '${binDir}'.")

        val environment = EnvironmentUtil.getEnv(
                project,
                "JDK_JAVA_OPTIONS",
                centralConfigServer.debugSuspend,
                centralConfigServer.debugPort,
                CentralConfigServerUtil.logFileName())

        project.logger.info("Starting Central Config Server with environment: $environment")

        val process = ProcessUtil.exec(mapOf(
                "command" to "run",
                "environment" to environment,
                "workDir" to binDir
        ))
        project.logger.lifecycle(
                "Central Config Server '${centralConfigServer.version}' successfully started on PID [${process.pid()}] with command [${
                    process.info().commandLine().orElse("")
                }].")
        waitForBoot(project, process, centralConfigServer)
    }

    fun waitForBoot(project: Project, process: Process, centralConfigServer: CentralConfigServer) {
        val server = DeployServerUtil.getServer(project)
        WaitForBootUtil.byLog(project,
                "Central Config Server ${centralConfigServer.version}",
                CentralConfigServerUtil.getCCLog(project, centralConfigServer),
                "Started ConfigServerApplication.",
                process,
                server.pingRetrySleepTime,
                server.pingTotalTries)
    }
}
