package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import java.io.File

class KubeCtlUtil {
    companion object {

        open class KubeContextInfo(val clusterName: String, val userName: String, val clusterServer: String, val clusterCertificateAuthorityData: String,
                               val clientCertificateData: String, val clientKeyData: String) {
        }

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

        fun getCurrentContextInfo(project: Project): KubeContextInfo {
            val context = getCurrentContext(project)
            val cluster = getContextCluster(project, context)
            val user = getContextUser(project, context)
            return KubeContextInfo(
                    cluster,
                    user,
                    getClusterServer(project, cluster),
                    getClusterCertificateAuthorityData(project, cluster),
                    getUserClientCertificateData(project, user),
                    getUserClientKeyData(project, user)
            )
        }

        fun getCurrentContext(project: Project): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config current-context")
        }

        fun getContextCluster(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.cluster}' --raw")
        }

        fun getContextUser(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.user}' --raw")
        }

        fun getClusterServer(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.server}' --raw")
        }

        fun getClusterCertificateAuthorityData(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}' --raw")
        }

        fun getUserClientKeyData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-key-data}' --raw")
        }

        fun getUserClientCertificateData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-certificate-data}' --raw")
        }

        private fun getAndGrep(project: Project, command: String, grepFor: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl get $command | grep $grepFor")
        }
    }
}
