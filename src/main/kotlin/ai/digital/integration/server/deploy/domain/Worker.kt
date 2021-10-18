package ai.digital.integration.server.deploy.domain

import ai.digital.integration.server.common.domain.api.Container
import ai.digital.integration.server.common.util.HTTPUtil

open class Worker(name: String) : Container(name) {
    var port: String = HTTPUtil.findFreePort().toString()
    var slimDistribution: Boolean = false
}
