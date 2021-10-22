package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.AkkaSecured
import ai.digital.integration.server.common.util.TlsUtil
import ai.digital.integration.server.deploy.domain.Satellite
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import org.gradle.api.Project

class SatelliteInitializeUtil {

    companion object {
        fun prepare(project: Project, satellite: Satellite) {
            val satelliteConf = SatelliteUtil.getSatelliteConf(project, satellite)

            val options = ConfigRenderOptions.concise().setFormatted(true).setJson(false)

            var newConfiguration = ConfigFactory.parseString(satelliteConf.readText(Charsets.UTF_8))
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

            if (DeployServerUtil.isAkkaSecured(project)) {
                val secured = TlsUtil.getAkkaSecured(project, DeployServerUtil.getServerWorkingDir(project))
                secured?.let {
                    val key = secured.keys[AkkaSecured.SATELLITE_KEY_NAME + satellite.name]

                    newConfiguration = newConfiguration
                        .withValue("deploy.server.ssl.enabled", ConfigValueFactory.fromAnyRef("yes"))
                        .withValue("deploy.server.ssl.key-store",
                            ConfigValueFactory.fromAnyRef(key?.keyStoreFile()?.absolutePath))
                        .withValue("deploy.server.ssl.key-store-password",
                            ConfigValueFactory.fromAnyRef(key?.keyStorePassword))
                        .withValue("deploy.server.ssl.trust-store",
                            ConfigValueFactory.fromAnyRef(secured.trustStoreFile().absolutePath))
                        .withValue("deploy.server.ssl.trust-store-password",
                            ConfigValueFactory.fromAnyRef(secured.truststorePassword))


                    if (AkkaSecured.KEYSTORE_TYPE != "pkcs12") {
                        newConfiguration = newConfiguration
                            .withValue("deploy.server.ssl.key-password",
                                ConfigValueFactory.fromAnyRef(key?.keyPassword))
                    }
                }
            }

            satelliteConf.writeText(
                newConfiguration
                    .root()
                    .render(options)
            )
        }
    }
}
