package ai.digital.integration.server.domain

import ai.digital.integration.server.util.HTTPUtil

class Worker {
    Integer debugPort
    Boolean debugSuspend = false
    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]
    Map<String, String> logLevels = new HashMap<>()
    String name
    Map<String, List<Object>> overlays = new HashMap<>()
    Integer port = HTTPUtil.findFreePort()
    String runtimeDirectory
    String outputFilename
    String version
    Boolean slimDistribution = true

    Worker(final String name) {
        this.name = name
    }

}

