package ai.digital.integration.server.util

import org.gradle.api.Project

import java.nio.file.Path

class MqUtil {

    static def RABBITMQ = 'rabbitmq'
    static def ACTIVEMQ = 'activemq'

    private MqUtil() {}

    static String getMqDirectory(Project project) {
        FileUtil.toPathString(LocationUtil.getServerDir(project), "mq")
    }

    static def mqName(project) {
        PropertyUtil.resolveValue(project, "mq", RABBITMQ)
    }

    static def mqPort(project) {
        PropertyUtil.resolveValue(project, "mqPort", null)
    }

    static def getMqRelativePath(project) {
        "mq/docker-compose_${mqName(project)}.yaml"
    }

    static def detectMqDependency(mq) {
        switch (mq) {
            case RABBITMQ: return rabbitmqPararms
            case ACTIVEMQ: return activemqPararms
            default: return rabbitmqPararms
        }
    }

    static Path getResolvedDockerFile(Project project) {
        def resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, getMqRelativePath(project))
        project.logger.lifecycle("Docker compose file for MQ is: $resultComposeFilePath")

        def mqTemplate = resultComposeFilePath.toFile()
        def port = mqName(project) == MqUtil.RABBITMQ ? 5672 : 61616

        def resolvedMqPort = PropertyUtil.resolveIntValue(project, "mqPort", port)

        def configuredTemplate = mqTemplate.text
                .replace('RABBITMQ_PORT2', "${resolvedMqPort}:5672")
                .replace('ACTIVEMQ_PORT2', "${resolvedMqPort}:61616")
        mqTemplate.text = configuredTemplate

        return resultComposeFilePath
    }

    static final MqParameters rabbitmqPararms = new MqParameters(
            "com.rabbitmq.jms:rabbitmq-jms",
            'com.rabbitmq.jms.admin.RMQConnectionFactory',
            null,
            "guest",
            "guest"
    )

    static final MqParameters activemqPararms = new MqParameters(
            "org.apache.activemq:activemq-client",
            'org.apache.activemq.ActiveMQConnectionFactory',
            null,
            "admin",
            "admin"
    )
}

class MqParameters {
    String driverDependency
    String driverClass
    String url
    String userName
    String password

    MqParameters(
            String driverDependency,
            String driverClass,
            String url,
            String userName,
            String password) {
        this.driverDependency = driverDependency
        this.driverClass = driverClass
        this.url = url
        this.userName = userName
        this.password = password
    }
}
