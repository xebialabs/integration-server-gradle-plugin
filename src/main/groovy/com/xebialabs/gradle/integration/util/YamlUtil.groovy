package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import org.gradle.internal.impldep.org.yaml.snakeyaml.DumperOptions
import org.gradle.internal.impldep.org.yaml.snakeyaml.Yaml

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class YamlUtil {

    private static void writeServerTaskYaml(project){
        def extension = ExtensionsUtil.getExtension(project)
        project.logger.lifecycle("Writing config to deploy-server.yaml file")
        def deployServerConfig = new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-server.yaml")
        def configStream = StartIntegrationServerTask.class.classLoader.getResourceAsStream("central-conf/deploy-server.yaml")
        Files.copy(configStream, Paths.get(deployServerConfig.toURI()), StandardCopyOption.REPLACE_EXISTING)
        DumperOptions options = new DumperOptions()
        options.setPrettyFlow(true)
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        Yaml parser = new Yaml(options)
        def serverConf = parser.load(deployServerConfig.text)

        if (project.hasProperty("externalWorker")) {
            serverConf['deploy.server']['port'] = extension.akkaRemotingPort
        }
        deployServerConfig.text = parser.dump(serverConf)
    }

    private static void writeDeployTaskYaml(project) {
        project.logger.lifecycle("Writing deploy-task.yaml file")
        def configStream = StartIntegrationServerTask.class.classLoader.getResourceAsStream("central-conf/deploy-task.yaml")
        def confDest = Paths.get("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-task.yaml")

        FileUtil.copyFile(configStream, confDest)

        DumperOptions options = new DumperOptions()
        options.setPrettyFlow(true)
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        Yaml parser = new Yaml(options)
        def taskConf = parser.load(confDest.text)

        if (project.hasProperty("externalWorker")) {
            taskConf['deploy.task']['in-process-worker'] = false
            taskConf['deploy.task'].queue.external = mq(MqUtil.mqName(project), MqUtil.mqPort(project))
            taskConf.akka = [io: [dns: [resolver: 'inet-address']]]
        } else {
            taskConf['deploy.task']['in-process-worker'] = true
            taskConf['deploy.task'].queue.external = []
        }
        confDest.text = parser.dump(taskConf)
    }

    private static def mq(mqName, mqPort) {

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
}
