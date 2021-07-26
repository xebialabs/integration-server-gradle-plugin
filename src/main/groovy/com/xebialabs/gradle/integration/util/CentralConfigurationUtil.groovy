package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class CentralConfigurationUtil {

    static def readServerKey(Project project, String key) {
        return FileUtil.readCCValue(project, "deploy-server.yaml", key)
    }
}
