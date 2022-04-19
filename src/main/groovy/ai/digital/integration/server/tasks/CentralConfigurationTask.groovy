package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CentralConfigurationTask extends DefaultTask {
    static NAME = "centralConfiguration"

    CentralConfigurationTask() {
        def dependencies = [
                DownloadAndExtractServerDistTask.NAME
        ]

        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private void overlayRepositoryConfig(String serverDir) {
        project.logger.lifecycle("Creating custom deploy-repository.yaml")

        def deployRepositoryYaml = new File("${serverDir}/centralConfiguration/deploy-repository.yaml")

        YamlFileUtil.writeFileValue(deployRepositoryYaml, DbUtil.dbConfig(project))

        if (DbUtil.isDerbyNetwork(project)) {
            def port = DbUtil.getDatabase(project).derbyPort.toString()
            def dbUrl = "jdbc:derby://localhost:$port/xldrepo;create=true;user=admin;password=admin".toString()

            YamlFileUtil.overlayFile(deployRepositoryYaml,
                    [
                            "xl.repository.database.db-url": dbUrl
                    ]
            )
        }
    }

    private void createCentralConfigurationFiles() {
        project.logger.lifecycle("Generating initial central configuration files")
        def serverDir = ServerUtil.getServerWorkingDir(project)

        overlayRepositoryConfig(serverDir)
        
        project.logger.lifecycle("Creating custom deploy-task.yaml")
        YamlFileUtil.overlayFile(
                new File("${serverDir}/centralConfiguration/deploy-task.yaml"),
                taskConfig(project))

        if (SatelliteUtil.hasSatellites(project)) {
            project.logger.lifecycle("Creating custom deploy-satellite.yaml")

            YamlFileUtil.overlayFile(
                    new File("${serverDir}/centralConfiguration/deploy-satellite.yaml"),
                    [
                            "deploy.satellite.enabled": true
                    ]
            )
        }
    }

    private static def taskConfig(Project project) {
        def mqDetail = mq(MqUtil.mqName(project), MqUtil.mqPort(project))
        def initial = [
                "deploy.task.queue.name"              : "xld-tasks-queue",
                "deploy.task.queue.archive-queue-name": "xld-archive-queue"
        ]

        initial.plus(WorkerUtil.hasWorkers(project) ?
                [
                        "deploy.task.in-process-worker"                  : false,
                        "deploy.task.queue.external.jms-driver-classname": mqDetail.get("jms-driver-classname"),
                        "deploy.task.queue.external.jms-password"        : mqDetail.get("jms-password"),
                        "deploy.task.queue.external.jms-url"             : mqDetail.get("jms-url"),
                        "deploy.task.queue.external.jms-username"        : mqDetail.get("jms-username"),
                        "akka.io.dns.resolver"                           : "inet-address"
                ] :
                ["deploy.task.in-process-worker": true])
    }

    static def mq(mqName, mqPort) {

        def hasPort = mqPort != null && (mqPort as Integer) > 0

        def rabbitMq = [
                "jms-driver-classname": "com.rabbitmq.jms.admin.RMQConnectionFactory",
                "jms-password"        : "guest",
                "jms-url"             : "amqp://localhost:${hasPort ? mqPort : "5672"}".toString(),
                "jms-username"        : "guest"
        ]

        def activeMq = [
                "jms-driver-classname": "org.apache.activemq.ActiveMQConnectionFactory",
                "jms-password"        : "admin",
                "jms-url"             : "tcp://localhost:${hasPort ? mqPort : "61616"}".toString(),
                "jms-username"        : "admin"
        ]

        "activemq" == mqName ? activeMq : rabbitMq
    }

    @TaskAction
    void launch() {
        createCentralConfigurationFiles()
    }
}
