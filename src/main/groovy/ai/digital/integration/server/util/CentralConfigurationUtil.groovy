package ai.digital.integration.server.util

import org.gradle.api.Project

class CentralConfigurationUtil {

    static def readServerKey(Project project, String key) {
        readCCValue(project, "deploy-server.yaml", key)
    }

    static def readCCValue(Project project, String fileName, String key) {
        def file = new File("${ServerUtil.getServerWorkingDir(project)}/centralConfiguration/$fileName")
        YamlFileUtil.readFileKey(file, key)
    }
}
