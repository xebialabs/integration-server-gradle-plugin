package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.tasks.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class StartSatelliteTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP

        this.dependsOn(DownloadAndExtractSatelliteDistTask.NAME)
        this.dependsOn(PrepareSatellitesTask.NAME)
        this.dependsOn(SatelliteOverlaysTask.NAME)
        this.dependsOn(SatelliteSyncPluginsTask.NAME)

        if (DeployServerUtil.isTls(project)) {
            this.dependsOn(TlsApplicationConfigurationOverrideTask.NAME)
        }
    }

    @TaskAction
    fun launch() {
        SatelliteUtil.getSatellites(project).forEach { satellite ->
            val binDir = SatelliteUtil.getBinDir(project, satellite)
            project.logger.lifecycle("Launching Satellite '${satellite.name} from ${binDir}'.")

            val environment = EnvironmentUtil.getEnv(
                project,
                "JDK_JAVA_OPTIONS",
                satellite.debugSuspend,
                satellite.debugPort,
                "xl-satellite.log"
            )
            project.logger.info("Starting worker with environment: $environment")
            val process = ProcessUtil.exec(mapOf(
                "command" to "run",
                "environment" to environment,
                "workDir" to binDir,
                "discardIO" to satellite.stdoutFileName.isNullOrEmpty(),
                "redirectTo" to if (!satellite.stdoutFileName.isNullOrEmpty()) File("${
                    SatelliteUtil.getSatelliteLogDir(project,
                        satellite)
                }/${satellite.stdoutFileName}") else null,
            ))
            project.logger.lifecycle(
                "Satellite '${satellite.name}' successfully started on PID [${process.pid()}] with command [${
                    process.info().commandLine().orElse("")
                }].")
            WaitForBootUtil.byLog(project,
                "Satellite ${satellite.name}",
                SatelliteUtil.getSatelliteLog(project, satellite),
                "XL Satellite has started",
                process)
        }
    }

    companion object {
        @JvmStatic
        val NAME = "startSatellite"
    }
}
