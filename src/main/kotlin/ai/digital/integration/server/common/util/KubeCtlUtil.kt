package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.InfrastructureInfo
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

        fun getCurrentContextInfo(project: Project, token: String? = null): InfrastructureInfo {
            val context = getCurrentContext(project)
            val cluster = getContextCluster(project, context)
            val user = getContextUser(project, context)
            return InfrastructureInfo(
                cluster,
                user,
                getClusterServer(project, cluster),
                token,
                getClusterCertificateAuthorityData(project, cluster),
                getUserClientCertificateData(project, user),
                getUserClientKeyData(project, user)
            )
        }

        private fun getCurrentContext(project: Project): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config current-context")
        }

        private fun getContextCluster(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.cluster}' --raw")
        }

        private fun getContextUser(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.user}' --raw")
        }

        private fun getClusterServer(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.server}' --raw")
        }

        private fun getClusterCertificateAuthorityData(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}' --raw")
        }

        private fun getUserClientKeyData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-key-data}' --raw")
        }

        private fun getUserClientCertificateData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-certificate-data}' --raw")
        }

        private fun getAndGrep(project: Project, command: String, grepFor: String): String {
            return ProcessUtil.executeCommand(project,
                "kubectl get $command | grep $grepFor")
        }
    }
}
