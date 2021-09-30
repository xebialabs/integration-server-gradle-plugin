package ai.digital.integration.util

import ai.digital.integration.server.util.MqParameters
import ai.digital.integration.server.util.MqUtil
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class MqUtilTest {

    @Test
    void detectMqDependencyTest() {
        MqParameters mqParams = MqUtil.detectMqDependency("rabbitmq")
        assertEquals("com.rabbitmq.jms:rabbitmq-jms", mqParams.getDriverDependency())
        assertEquals("com.rabbitmq.jms.admin.RMQConnectionFactory", mqParams.getDriverClass())
        assertEquals(null, mqParams.getUrl())
        assertEquals("guest", mqParams.getUserName())
        assertEquals("guest", mqParams.getPassword())

        mqParams = MqUtil.detectMqDependency("activemq")
        assertEquals("org.apache.activemq:activemq-client", mqParams.getDriverDependency())
        assertEquals("org.apache.activemq.ActiveMQConnectionFactory", mqParams.getDriverClass())
        assertEquals(null, mqParams.getUrl())
        assertEquals("admin", mqParams.getUserName())
        assertEquals("admin", mqParams.getPassword())
    }
}
