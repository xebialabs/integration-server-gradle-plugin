package ai.digital.integration.util

import ai.digital.integration.server.util.HTTPUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HTTPUtilTest {

    @Test
    void findFreePortTest() {
        int port = HTTPUtil.findFreePort()
        Assertions.assertNotNull(port)
    }
}
