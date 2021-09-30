package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.Engine
import ai.digital.integration.server.util.HTTPUtil

class Worker(name: String) : Engine(name) {
    val port: String = HTTPUtil.findFreePort().toString()
    val slimDistribution: Boolean = false
}
