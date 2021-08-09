package ai.digital.integration.server.domain

import ai.digital.integration.server.util.HTTPUtil

class Worker {

    Integer debugPort

    Boolean debugSuspend = false

    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]

    String name

    Integer port = HTTPUtil.findFreePort()

    String directory

    Worker(final String name) {
        this.name = name
    }

}

