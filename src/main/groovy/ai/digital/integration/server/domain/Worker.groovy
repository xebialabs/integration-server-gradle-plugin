package ai.digital.integration.server.domain

import ai.digital.integration.server.util.HTTPUtil

class Worker {
    Integer debugPort
    Boolean debugSuspend = false
    String runtimeDirectory
    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
    String name
    Integer port = HTTPUtil.findFreePort()
    String version
    Map<String, String> logLevels = new HashMap<>()
    Map<String, List<Object>> overlays = new HashMap<>()

    Worker(final String name) {
        this.name = name
    }

}

