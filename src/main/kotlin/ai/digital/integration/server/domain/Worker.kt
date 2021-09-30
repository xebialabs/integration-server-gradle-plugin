package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.Engine
import ai.digital.integration.server.util.HTTPUtil

open class Worker(name: String) : Engine(name) {
    var port: String = HTTPUtil.findFreePort().toString()
    var slimDistribution: Boolean = false
}
