package ai.xebialabs.gradle.integration.util

import ai.digital.integration.server.util.DbConfigurationUtil
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class DbConfigurationUtilTest {

    @Test
    void connectionPropertiesTest() {
        Properties properties = DbConfigurationUtil.connectionProperties("postgres", "demo")
        assertEquals("postgres", properties.get("user"))
        assertEquals("demo", properties.get("password"))
    }
}
