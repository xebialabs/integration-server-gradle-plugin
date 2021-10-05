package ai.digital.integration.util

import ai.digital.integration.server.domain.MqParameters
import ai.digital.integration.server.util.MqUtil
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class MqUtilTest {

    @Test
    void detectMqDependencyTest() {
        MqParameters mqParams = MqUtil.detectMqDependency("rabbitmq")
        assertEquals("com.rabbitmq.jms:rabbitmq-jms", mqParams.driverDependency)
        assertEquals("com.rabbitmq.jms.admin.RMQConnectionFactory", mqParams.driverClass)
        assertEquals(null, mqParams.url)
        assertEquals("guest", mqParams.userName)
        assertEquals("guest", mqParams.password)

        mqParams = MqUtil.detectMqDependency("activemq")
        assertEquals("org.apache.activemq:activemq-client", mqParams.driverDependency)
        assertEquals("org.apache.activemq.ActiveMQConnectionFactory", mqParams.driverClass)
        assertEquals(null, mqParams.url)
        assertEquals("admin", mqParams.userName)
        assertEquals("admin", mqParams.password)
    }
}
