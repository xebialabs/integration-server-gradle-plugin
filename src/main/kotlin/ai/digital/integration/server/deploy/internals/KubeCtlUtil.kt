package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import java.io.File

class KubeCtlUtil {
    companion object {

        fun apply(project: Project, file: File) {
            ProcessUtil.executeCommand(project,
                    "kubectl apply -f ${file.absolutePath}")
        }

        fun setDefaultStorageClass(project: Project, oldDefaultStorageClass: String, newDefaultStorageClass: String) {
            ProcessUtil.executeCommand(project,
                    " kubectl patch storageclass $newDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"true\"}}}'")
            ProcessUtil.executeCommand(project,
                    " kubectl patch storageclass $oldDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"false\"}}}'")
        }

        fun hasStorageClass(project: Project, storageClass: String): Boolean {
            val result = getAndGrep(project, "storageclass", storageClass)
            project.logger.lifecycle("RESULT: {} {}", result, result.contains(storageClass))
            return result.contains(storageClass)
        }

        private fun getAndGrep(project: Project, command: String, grepFor: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl get $command | grep $grepFor")
        }
    }
}
