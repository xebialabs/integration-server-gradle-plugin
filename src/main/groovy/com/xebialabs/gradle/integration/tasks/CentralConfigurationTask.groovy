package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.MqUtil
import com.xebialabs.gradle.integration.util.YamlFileUtil

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class CentralConfigurationTask extends DefaultTask {
    static NAME = "centralConfiguration"

    private void createCentralConfigurationFiles() {
        project.logger.lifecycle("Generating initial central configuration files")

        def extension = ExtensionsUtil.getExtension(project)
        project.logger.lifecycle("Writing to deploy-repository file")
        YamlFileUtil.writeFileValue(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-repository.yaml"),
                DbUtil.dbConfig(project))

        project.logger.lifecycle("Writing to deploy-server file")
        YamlFileUtil.overlayFile(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-server.yaml"),
                ["deploy.server.port": extension.akkaRemotingPort]
        )

        project.logger.lifecycle("Writing to deploy-task file")
        YamlFileUtil.overlayFile(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-task.yaml"),
                taskConfig(project))
    }

    private def taskConfig(project) {
        def taskConfigStream = YamlFileUtil.class.classLoader.getResourceAsStream("central-conf/deploy-task.yaml")
        Files.copy(taskConfigStream, Paths.get(new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-task.yaml").toURI()), StandardCopyOption.REPLACE_EXISTING)
        Map taskConf = new LinkedHashMap<String, Object>()
        if (project.hasProperty("externalWorker")) {
            taskConf.put("deploy.task.in-process-worker", false)
            def mqDetail = mq(MqUtil.mqName(project), MqUtil.mqPort(project))
            taskConf.put("deploy.task.queue.external.jms-driver-classname", mqDetail.get("jms-driver-classname"))
            taskConf.put("deploy.task.queue.external.jms-password", mqDetail.get("jms-password"))
            taskConf.put("deploy.task.queue.external.jms-url", mqDetail.get("jms-url"))
            taskConf.put("deploy.task.queue.external.jms-username", mqDetail.get("jms-username"))
        } else {
            taskConf.put("deploy.task.in-process-worker", true)
        }
        taskConf
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
