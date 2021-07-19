package com.xebialabs.gradle.integration.util

class YamlPatchUtil {

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


   static def configFile(filename) {
        return YamlPatchUtil.class.classLoader.getResourceAsStream("central-conf/${filename}.yaml")
    }

    static def serverConfig(project) {
        project.logger.info("Writing to deploy-server.yaml")
        def extension =ExtensionsUtil.getExtension(project)
        def serverConf = YamlUtil.mapper.readTree(configFile("deploy-server"))
        serverConf.put("deploy.server.port", extension.getAkkaRemotingPort())
        YamlUtil.mapper.writeValue(
             new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-server.yaml"),
             serverConf)
    }

    static def taskConfig(project){
        project.logger.info("Writing to deploy-task.yaml")
        def taskConf = YamlUtil.mapper.readTree(configFile( "deploy-task"))
        if (project.hasProperty("externalWorker")) {
            taskConf.put("deploy.task.in-process-worker", false)
            def mqDetail = mq(MqUtil.mqName(project), MqUtil.mqPort(project))
            taskConf.put("deploy.task.queue.external.jms-driver-classname",mqDetail.get("jms-driver-classname"))
            taskConf.put("deploy.task.queue.external.jms-password",mqDetail.get("jms-password"))
            taskConf.put("deploy.task.queue.external.jms-url",mqDetail.get("jms-url"))
            taskConf.put("deploy.task.queue.external.jms-username",mqDetail.get("jms-username"))
        } else {
            taskConf.put("deploy.task.in-process-worker", true)
        }
        YamlUtil.mapper.writeValue(
                new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration/deploy-task.yaml"),
                taskConf)
    }
}
