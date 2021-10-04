package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Satellite
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import org.gradle.api.Project

class SatelliteInitializeUtil {

    companion object {
        @JvmStatic
        fun prepare(project: Project, satellite: Satellite) {
            val satelliteConf = SatelliteUtil.getSatelliteConf(project, satellite)

            val options = ConfigRenderOptions.concise().setFormatted(true).setJson(false)

            val newConfiguration = ConfigFactory.parseString(satelliteConf.readText(Charsets.UTF_8))
                .withValue(
                    "deploy.server.bind-hostname",
                    ConfigValueFactory.fromAnyRef(satellite.serverAkkaBindHostName)
                )
                .withValue("deploy.server.hostname", ConfigValueFactory.fromAnyRef(satellite.serverAkkaHostname))
                .withValue("deploy.server.port", ConfigValueFactory.fromAnyRef(satellite.serverAkkaPort))
                .withValue("deploy.satellite.metrics.port", ConfigValueFactory.fromAnyRef(satellite.metricsPort))
                .withValue(
                    "deploy.satellite.streaming.port",
                    ConfigValueFactory.fromAnyRef(satellite.akkaStreamingPort)
                )
                .root()
                .render(options)

            satelliteConf.writeText(newConfiguration)
        }
    }
}
