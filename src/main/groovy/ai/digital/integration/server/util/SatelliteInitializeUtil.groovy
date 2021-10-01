package ai.digital.integration.server.util

import ai.digital.integration.server.domain.AkkaSecured
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


        if (ServerUtil.isAkkaSecured(project)) {
            def secured = SslUtil.getAkkaSecured(project, ServerUtil.getServerWorkingDir(project))
            def key = secured.keys[AkkaSecured.SATELLITE_KEY_NAME + satellite.name]

            newConfiguration = newConfiguration
                .withValue("deploy.server.ssl.enabled", ConfigValueFactory.fromAnyRef("yes"))
                .withValue("deploy.server.ssl.key-store", ConfigValueFactory.fromAnyRef(key.keyStoreFile().absolutePath))
                .withValue("deploy.server.ssl.key-store-password", ConfigValueFactory.fromAnyRef(key.keyStorePassword))
                .withValue("deploy.server.ssl.trust-store", ConfigValueFactory.fromAnyRef(secured.trustStoreFile().absolutePath))
                .withValue("deploy.server.ssl.trust-store-password", ConfigValueFactory.fromAnyRef(secured.truststorePassword))


            if (AkkaSecured.KEYSTORE_TYPE != "pkcs12") {
                newConfiguration = newConfiguration
                    .withValue("deploy.server.ssl.key-password", ConfigValueFactory.fromAnyRef(key.keyPassword))
            }
        }

        satelliteConf.text = newConfiguration
            .root()
            .render(options)
    }
}
