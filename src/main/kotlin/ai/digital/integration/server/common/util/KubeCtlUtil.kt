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

        fun wait(project: Project, resource: String, condition: String, timeoutSeconds: Int) {
            ProcessUtil.executeCommand(project,
                    "kubectl wait --for condition=$condition --timeout=${timeoutSeconds}s $resource")
        }

        fun setDefaultStorageClass(project: Project, oldDefaultStorageClass: String, newDefaultStorageClass: String) {
            ProcessUtil.executeCommand(project,
                    " kubectl patch storageclass $newDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"true\"}}}'")
            ProcessUtil.executeCommand(project,
                    " kubectl patch storageclass $oldDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"false\"}}}'")
        }

        fun hasStorageClass(project: Project, storageClass: String): Boolean {
            val result = getAndGrep(project, "storageclass", storageClass)
            return result.contains(storageClass)
        }

        fun getCurrentContextInfo(project: Project): InfrastructureInfo {
            val context = getCurrentContext(project)
            val cluster = getContextCluster(project, context)
            val user = getContextUser(project, context)
            return InfrastructureInfo(
                    cluster,
                    user,
                    getClusterServer(project, cluster),
                    getClusterCertificateAuthorityData(project, cluster),
                    getUserClientCertificateData(project, user),
                    getUserClientKeyData(project, user)
            )
        }

        fun deleteCurrentContext(project: Project) {
            val context = getCurrentContext(project)
            val cluster = getContextCluster(project, context)
            val user = getContextUser(project, context)

            project.logger.info("Delete current context {} with related cluster {} and user {} information", context, cluster, user)
            ProcessUtil.executeCommand(project,
                    "kubectl config delete-context $context")
            ProcessUtil.executeCommand(project,
                    "kubectl config delete-user $user")
            ProcessUtil.executeCommand(project,
                    "kubectl config delete-cluster $cluster")
        }

        fun getCurrentContext(project: Project): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config current-context", logOutput = false)
        }

        fun getContextCluster(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.cluster}' --raw", logOutput = false)
        }

        fun getContextUser(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.user}' --raw", logOutput = false)
        }

        fun getClusterServer(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.server}' --raw", logOutput = false)
        }

        fun getClusterCertificateAuthorityData(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}' --raw", logOutput = false)
        }

        fun getUserClientKeyData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-key-data}' --raw", logOutput = false)
        }

        fun getUserClientCertificateData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-certificate-data}' --raw", logOutput = false)
        }

        private fun getAndGrep(project: Project, command: String, grepFor: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl get $command | grep $grepFor")
        }
    }
}
