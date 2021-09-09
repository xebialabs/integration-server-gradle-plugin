package ai.digital.integration.server.domain

import ai.digital.integration.server.domain.api.Engine
import ai.digital.integration.server.util.HTTPUtil

class Worker extends Engine {
    String name
    Integer port = HTTPUtil.findFreePort()
    Boolean slimDistribution = false

    Worker(final String name) {
        this.name = name
    }

}

