package ai.digital.integration.server.common.domain

import ai.digital.integration.server.common.util.HTTPUtil

class OperatorServer {
    var dockerImage: String? = "xebialabs/xl-deploy"
    var httpPort: Int = HTTPUtil.findFreePort()
    var pingRetrySleepTime: Int = 1
    var pingTotalTries: Int = 200
    var version: String? = null
}
