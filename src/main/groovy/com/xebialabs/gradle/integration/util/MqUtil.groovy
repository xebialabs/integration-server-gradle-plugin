package com.xebialabs.gradle.integration.util

class MqUtil {

    static def RABBITMQ = 'rabbitmq'
    static def ACTIVEMQ = 'activemq'

    private MqUtil() {}

    static def mqName(project) {
        project.hasProperty("mq") ? project.property("mq").toString() : RABBITMQ
    }

    static def mqPort(project) {
        project.hasProperty("mqPort") ? project.property("mqPort") : null
    }

    static def getMqFileName(project){
        "mq/docker-compose_${mqName(project)}.yaml"
    }

    static def detectMqDependency(mq) {
        switch (mq) {
            case RABBITMQ: return rabbitmqPararms
            case ACTIVEMQ: return activemqPararms
            default: return rabbitmqPararms
        }
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