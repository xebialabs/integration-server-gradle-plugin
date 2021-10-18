package ai.digital.integration.server.util

import ai.digital.integration.server.common.util.DbConfigurationUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DbConfigurationUtilTest {

    @Test
    fun connectionPropertiesTest() {
        val properties = DbConfigurationUtil.connectionProperties("postgres", "demo")
        assertEquals("postgres", properties.get("user"))
        assertEquals("demo", properties.get("password"))
    }
}
