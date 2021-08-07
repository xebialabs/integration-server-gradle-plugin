package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class CentralConfigurationUtil {

    static def readServerKey(Project project, String key) {
        return readCCValue(project, "deploy-server.yaml", key)
    }

    static def readCCValue(Project project, String fileName, String key) {
        def file = new File("${LocationUtil.getServerWorkingDir(project)}/centralConfiguration/$fileName")
        return YamlFileUtil.readFileKey(file, key)
    }
}
