package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.util.ExtensionsUtil

class Worker {

    Integer debugPort

    Boolean debugSuspend = false

    String[] jvmArgs = ["-Xmx1024m", "-Duser.timezone=UTC"]

    String name

    Integer port = ExtensionsUtil.findFreePort()

    String directory

    Worker(final String name) {
        this.name = name
    }

}

