package ai.digital.integration.server.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HTTPUtilTest {

    @Test
    fun findFreePortTest() {
        val port = HTTPUtil.findFreePort()
        Assertions.assertNotNull(port)
    }
}
