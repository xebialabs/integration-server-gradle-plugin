package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.InfrastructureInfo
import org.gradle.api.Project
import java.io.File

class KubeCtlUtil {
    companion object {

        fun apply(file: File) {
            ProcessUtil.executeCommand("kubectl apply -f ${file.absolutePath}")
        }

        fun setDefaultStorageClass(oldDefaultStorageClass: String, newDefaultStorageClass: String) {
            ProcessUtil.executeCommand(" kubectl patch storageclass $newDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"true\"}}}'")
            ProcessUtil.executeCommand(" kubectl patch storageclass $oldDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"false\"}}}'")
        }

        fun hasStorageClass(project: Project, storageClass: String): Boolean {
            val result = getAndGrep("storageclass", storageClass)
            project.logger.lifecycle("RESULT: {} {}", result, result.contains(storageClass))
            return result.contains(storageClass)
        }

        fun getCurrentContextInfo(token: String? = null): InfrastructureInfo {
            val context = getCurrentContext()
            val cluster = getContextCluster(context)
            val user = getContextUser(context)
            return InfrastructureInfo(
                cluster,
                user,
                getClusterServer(cluster),
                token,
                getClusterCertificateAuthorityData(cluster),
                getUserClientCertificateData(user),
                getUserClientKeyData(user)
            )
        }

        private fun getCurrentContext(): String {
            return ProcessUtil.executeCommand("kubectl config current-context")
        }

        private fun getContextCluster(contextName: String): String {
            return ProcessUtil.executeCommand("kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.cluster}' --raw")
        }

        private fun getContextUser(contextName: String): String {
            return ProcessUtil.executeCommand("kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.user}' --raw")
        }

        private fun getClusterServer(clusterName: String): String {
            return ProcessUtil.executeCommand("kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.server}' --raw")
        }

        private fun getClusterCertificateAuthorityData(clusterName: String): String {
            return ProcessUtil.executeCommand("kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}' --raw")
        }

        private fun getUserClientKeyData(userName: String): String {
            return ProcessUtil.executeCommand("kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-key-data}' --raw")
        }

        private fun getUserClientCertificateData(userName: String): String {
            return ProcessUtil.executeCommand("kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-certificate-data}' --raw")
        }

        private fun getAndGrep(command: String, grepFor: String): String {
            return ProcessUtil.executeCommand("kubectl get $command | grep $grepFor")
        }
    }
}
