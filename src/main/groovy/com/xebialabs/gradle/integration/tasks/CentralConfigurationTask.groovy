package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.MqUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import com.xebialabs.gradle.integration.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CentralConfigurationTask extends DefaultTask {
    static NAME = "centralConfiguration"

    private void createCentralConfigurationFiles() {
        project.logger.lifecycle("Generating initial central configuration files")

        project.logger.lifecycle("Writing to deploy-repository file")
        YamlFileUtil.writeFileValue(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-repository.yaml"),
                DbUtil.dbConfig(project))

        project.logger.lifecycle("Writing to deploy-server file")
        YamlFileUtil.overlayFile(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-server.yaml"),
                [
                        "deploy.server.port"    : ExtensionsUtil.findFreePort(),
                        "deploy.server.hostname": "127.0.0.1"
                ]
        )

        project.logger.lifecycle("Writing to deploy-task file")
        YamlFileUtil.overlayFile(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-task.yaml"),
                taskConfig(project))
    }

    private static def taskConfig(project) {
        def mqDetail = mq(MqUtil.mqName(project), MqUtil.mqPort(project))
        def initial = [
                "deploy.task.queue.name"              : "xld-tasks-queue",
                "deploy.task.queue.archive-queue-name": "xld-archive-queue"
        ]
        return initial.plus(WorkerUtil.hasWorkers(project) ?
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

        return "activemq" == mqName ? activeMq : rabbitMq
    }

    @TaskAction
    void launch() {
        createCentralConfigurationFiles()
    }
}
