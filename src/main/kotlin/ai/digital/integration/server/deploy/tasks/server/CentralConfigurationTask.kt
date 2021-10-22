package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.domain.AkkaSecured
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CentralConfigurationTask : DefaultTask() {

    companion object {
        const val NAME = "centralConfiguration"
    }

    init {
        this.dependsOn(DownloadAndExtractServerDistTask.NAME)
        this.group = PLUGIN_GROUP
    }

    private fun overlayRepositoryConfig(serverDir: String) {
        project.logger.lifecycle("Creating custom deploy-repository.yaml")

        val deployRepositoryYaml = File("${serverDir}/centralConfiguration/deploy-repository.yaml")

        DbUtil.dbConfig(project)?.let { config ->
            YamlFileUtil.writeFileValue(deployRepositoryYaml, config)
        }

        if (DbUtil.isDerbyNetwork(project)) {
            val port = DbUtil.getDatabase(project).derbyPort
            val dbUrl = "jdbc:derby://localhost:$port/xldrepo;create=true;user=admin;password=admin"
            YamlFileUtil.overlayFile(deployRepositoryYaml, mutableMapOf("xl.repository.database.db-url" to dbUrl))
        }
    }

    private fun createCentralConfigurationFiles(server: Server) {
        project.logger.lifecycle("Generating initial central configuration files for server ${server.name}")
        val serverDir = DeployServerUtil.getServerWorkingDir(project, server)

        overlayRepositoryConfig(serverDir)

        project.logger.lifecycle("Creating custom deploy-server.yaml")

        val serverYaml: MutableMap<String, Any> = mutableMapOf(
            "deploy.server.port" to HTTPUtil.findFreePort(),
            "deploy.server.hostname" to "127.0.0.1"
        )

        if (DeployServerUtil.isAkkaSecured(project)) {
            val secured = TlsUtil.getAkkaSecured(project, DeployServerUtil.getServerWorkingDir(project))

            secured?.let { sec ->
                val key = sec.keys[AkkaSecured.MASTER_KEY_NAME + DeployServerUtil.getServer(project).name]
                if (key != null) {
                    serverYaml.putAll(
                        mutableMapOf(
                            "deploy.server.ssl.enabled" to true,
                            "deploy.server.ssl.key-store" to key.keyStoreFile().absolutePath,
                            "deploy.server.ssl.key-store-password" to key.keyStorePassword,
                            "deploy.server.ssl.trust-store" to sec.trustStoreFile().absolutePath,
                            "deploy.server.ssl.trust-store-password" to sec.truststorePassword,
                        ))
                    if (AkkaSecured.KEYSTORE_TYPE != "pkcs12") {
                        serverYaml["deploy.server.ssl.key-password"] = key.keyPassword
                    }
                }
            }

        }

        YamlFileUtil.overlayFile(
            File("${serverDir}/centralConfiguration/deploy-server.yaml"),
            serverYaml
        )

        project.logger.lifecycle("Creating custom deploy-task.yaml")
        YamlFileUtil.overlayFile(
            File("${serverDir}/centralConfiguration/deploy-task.yaml"),
            taskConfig(project))

        if (SatelliteUtil.hasSatellites(project)) {
            project.logger.lifecycle("Creating custom deploy-satellite.yaml")

            YamlFileUtil.overlayFile(
                File("${serverDir}/centralConfiguration/deploy-satellite.yaml"),
                mutableMapOf(
                    "deploy.satellite.enabled" to true
                )
            )
        }
    }

    private fun taskConfig(project: Project): MutableMap<String, Any> {
        val mqDetail = mq(MqUtil.mqName(project), MqUtil.mqPort(project))

        val config = mutableMapOf<String, Any>(
            "deploy.task.queue.name" to "xld-tasks-queue",
            "deploy.task.queue.archive-queue-name" to "xld-archive-queue"
        )

        config.putAll(
            if (WorkerUtil.hasWorkers(project))
                mutableMapOf(
                    "deploy.task.in-process-worker" to "false",
                    "deploy.task.queue.external.jms-driver-classname" to mqDetail["jms-driver-classname"].toString(),
                    "deploy.task.queue.external.jms-password" to mqDetail["jms-password"].toString(),
                    "deploy.task.queue.external.jms-url" to mqDetail["jms-url"].toString(),
                    "deploy.task.queue.external.jms-username" to mqDetail["jms-username"].toString(),
                    "akka.io.dns.resolver" to "inet-address"
                ) else
                mutableMapOf(
                    "deploy.task.in-process-worker" to "true"
                )
        )
        return config
    }

    private fun mq(mqName: String?, mqPort: Int?): MutableMap<String, String> {

        val hasPort = mqPort != null && mqPort > 0

        val rabbitMq = mutableMapOf(
            "jms-driver-classname" to "com.rabbitmq.jms.admin.RMQConnectionFactory",
            "jms-password" to "guest",
            "jms-url" to "amqp://localhost:${if (hasPort) mqPort else "5672"}",
            "jms-username" to "guest"
        )

        val activeMq = mutableMapOf(
            "jms-driver-classname" to "org.apache.activemq.ActiveMQConnectionFactory",
            "jms-password" to "admin",
            "jms-url" to "tcp://localhost:${if (hasPort) mqPort else "61616"}",
            "jms-username" to "admin"
        )

        return if ("activemq" == mqName) activeMq else rabbitMq
    }

    @TaskAction
    fun launch() {
        DeployServerUtil.getServers(project)
                .forEach { server ->
                    createCentralConfigurationFiles(server)
                }
    }
}
