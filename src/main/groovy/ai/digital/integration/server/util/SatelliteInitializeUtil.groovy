package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Satellite
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import org.gradle.api.Project

class SatelliteInitializeUtil {

    static def prepare(Project project, Satellite satellite) {
        def satelliteConf = SatelliteUtil.getSatelliteConf(project, satellite)

        def options = ConfigRenderOptions.concise().setFormatted(true).setJson(false)

        def newConfiguration = ConfigFactory.parseString(satelliteConf.text)
                .withValue("deploy.server.bind-hostname", ConfigValueFactory.fromAnyRef(satellite.serverAkkaBindHostName))
                .withValue("deploy.server.hostname", ConfigValueFactory.fromAnyRef(satellite.serverAkkaHostname))
                .withValue("deploy.server.port", ConfigValueFactory.fromAnyRef(satellite.serverAkkaPort))
                .withValue("deploy.satellite.metrics.port", ConfigValueFactory.fromAnyRef(satellite.metricsPort))
                .withValue("deploy.satellite.streaming.port", ConfigValueFactory.fromAnyRef(satellite.akkaStreamingPort))
                .root()
                .render(options)

        satelliteConf.text = newConfiguration
    }
}
