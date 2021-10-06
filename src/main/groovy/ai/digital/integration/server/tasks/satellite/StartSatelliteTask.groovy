package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.tasks.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.EnvironmentUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.SatelliteUtil
import ai.digital.integration.server.util.WaitForBootUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class StartSatelliteTask extends DefaultTask {
    public static String NAME = "startSatellite"

    StartSatelliteTask() {
        def dependencies = [
                DownloadAndExtractSatelliteDistTask.NAME,
                PrepareSatellitesTask.NAME,
                SatelliteOverlaysTask.NAME
        ]

        if (DeployServerUtil.isTls(project)) {
            dependencies += [ TlsApplicationConfigurationOverrideTask.NAME ]
        }

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void launch() {
        SatelliteUtil.getSatellites(project).each { Satellite satellite ->
            def binDir = SatelliteUtil.getBinDir(project, satellite)
            project.logger.lifecycle("Launching Satellite '${satellite.name} from ${binDir}'.")

            def environment = EnvironmentUtil.getEnv(
                project,
                "SATELLITE_OPTS",
                satellite.debugSuspend,
                satellite.debugPort,
                "xl-satellite.log"
            )
            project.logger.info("Starting worker with environment: $environment")
            Process process = ProcessUtil.exec([
                    command    : "run",
                    environment: environment,
                    workDir    : binDir,
                    discardIO  : satellite.stdoutFileName ? false : true,
                    redirectTo : satellite.stdoutFileName ? new File("${SatelliteUtil.getSatelliteLogDir(project, satellite)}/${satellite.stdoutFileName}") : null,
            ])
            project.logger.lifecycle("Satellite '${satellite.name}' successfully started on PID [${process.pid()}] with command [${process.info().commandLine().orElse("")}].")
            WaitForBootUtil.byLog(project, "Satellite ${satellite.name}", SatelliteUtil.getSatelliteLog(project, satellite), "XL Satellite has started", process)
        }
    }
}
