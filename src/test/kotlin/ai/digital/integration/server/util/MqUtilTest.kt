package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.MqUtil
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class MqUtilTest {

    @Test
    fun detectMqDependencyTest() {
        val rabbitMqParams = MqUtil.detectMqDependency("rabbitmq")
        assertEquals("com.rabbitmq.jms:rabbitmq-jms", rabbitMqParams.driverDependency)
        assertEquals("com.rabbitmq.jms.admin.RMQConnectionFactory", rabbitMqParams.driverClass)
        assertEquals(null, rabbitMqParams.url)
        assertEquals("guest", rabbitMqParams.userName)
        assertEquals("guest", rabbitMqParams.password)

        val activeMqParams = MqUtil.detectMqDependency("activemq")
        assertEquals("org.apache.activemq:activemq-client", activeMqParams.driverDependency)
        assertEquals("org.apache.activemq.ActiveMQConnectionFactory", activeMqParams.driverClass)
        assertEquals(null, activeMqParams.url)
        assertEquals("admin", activeMqParams.userName)
        assertEquals("admin", activeMqParams.password)
    }
}
