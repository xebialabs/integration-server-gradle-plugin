package ai.digital.integration.server.common.util

import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.Project
import java.io.File

class CentralConfigurationUtil {
    companion object {
        fun readServerKey(project: Project, key: String): String {
            return readCCValue(project, "deploy-server.yaml", key).toString()
        }

        private fun readCCValue(project: Project, fileName: String, key: String): Any? {
            val file = File("${DeployServerUtil.getServerWorkingDir(project)}/centralConfiguration/$fileName")
            return YamlFileUtil.readFileKey(file, key)
        }
    }
}
