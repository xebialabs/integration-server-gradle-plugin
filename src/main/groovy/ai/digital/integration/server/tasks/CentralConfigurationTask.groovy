package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.HTTPUtil
import ai.digital.integration.server.util.LocationUtil
import ai.digital.integration.server.util.MqUtil
import ai.digital.integration.server.util.WorkerUtil
import ai.digital.integration.server.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class CentralConfigurationTask extends DefaultTask {
    static NAME = "centralConfiguration"

    private void createCentralConfigurationFiles() {
        project.logger.lifecycle("Generating initial central configuration files")
        def serverDir = LocationUtil.getServerWorkingDir(project)

        project.logger.lifecycle("Creating custom deploy-repository.yaml")
        YamlFileUtil.writeFileValue(
                new File("${serverDir}/centralConfiguration/deploy-repository.yaml"),
                DbUtil.dbConfig(project))

        project.logger.lifecycle("Creating custom deploy-server.yaml")
        YamlFileUtil.overlayFile(
                new File("${serverDir}/centralConfiguration/deploy-server.yaml"),
                [
                        "deploy.server.port"    : HTTPUtil.findFreePort(),
                        "deploy.server.hostname": "127.0.0.1"
                ]
        )

        project.logger.lifecycle("Creating custom deploy-task.yaml")
        YamlFileUtil.overlayFile(
                new File("${serverDir}/centralConfiguration/deploy-task.yaml"),
                taskConfig(project))
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
