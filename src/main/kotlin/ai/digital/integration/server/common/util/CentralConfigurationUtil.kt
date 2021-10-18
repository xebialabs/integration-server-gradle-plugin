package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.util.DeployServerUtil
import org.gradle.api.Project
import java.io.File

class CentralConfigurationUtil {
    companion object {
        @JvmStatic
        fun readServerKey(project: Project, key: String): String {
            return readCCValue(project, "deploy-server.yaml", key).toString()
        }

        @JvmStatic
        fun readCCValue(project: Project, fileName: String, key: String): Any? {
            val file = File("${DeployServerUtil.getServerWorkingDir(project)}/centralConfiguration/$fileName")
            return YamlFileUtil.readFileKey(file, key)
        }
    }
}
