package com.xebialabs.gradle.integration.domain

import com.xebialabs.gradle.integration.util.HTTPUtil

class Server {

    String contextRoot = "/"

    Boolean debugSuspend = false

    Integer debugPort

    Integer httpPort = HTTPUtil.findFreePort()

    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]

    Map<String, String> logLevels = new HashMap<>()

    String name

    Map<String, List<Object>> overlays = new HashMap<>()

    Integer pingRetrySleepTime = 10

    Integer pingTotalTries = 60

    String provisionScript

    Integer provisionSocketTimeout = 60000

    Boolean removeStdoutConfig = false

    String runtimeDirectory

    String version

    Map<String, Map<String, Object>> yamlPatches = new HashMap<>()

    Server(final String name) {
        this.name = name
    }
}
