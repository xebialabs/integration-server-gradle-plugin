package ai.xebialabs.gradle.integration.util

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class DbConfigurationUtilTest {

    @Test
    void connectionPropertiesTest() {
        Properties properties = getConnectionProperties()
        assertEquals("postgres", properties.get("user"))
        assertEquals("demo", properties.get("password"))
    }
}
