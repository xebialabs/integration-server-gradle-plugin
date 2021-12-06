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

        fun delete(project: Project, file: File) {
            ProcessUtil.executeCommand( project,"kubectl delete -f ${file.absolutePath}")
        }

        fun wait(project: Project, resource: String, condition: String, timeoutSeconds: Int): Boolean {
            project.logger.lifecycle("Waiting for resource $resource to be $condition")
            val expectedEndTime = System.currentTimeMillis()+ timeoutSeconds * 1000
            while (expectedEndTime > System.currentTimeMillis()) {
                val result = ProcessUtil.executeCommand(project,
                        "kubectl wait --for condition=$condition --timeout=${timeoutSeconds}s $resource", throwErrorOnFailure = false)
                if (result.contains("condition met")) {
                    return true
                }
                Thread.sleep(1000)
            }
            return false
        }

        fun setDefaultStorageClass(project: Project, oldDefaultStorageClass: String, newDefaultStorageClass: String) {
            ProcessUtil.executeCommand(project,
                    " kubectl patch storageclass $newDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"true\"}}}'")
            ProcessUtil.executeCommand(project,
                    " kubectl patch storageclass $oldDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"false\"}}}'")
        }

        fun hasStorageClass(project: Project, storageClass: String): Boolean {
            val result = getNameAndGrep(project, "storageclass", storageClass)
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

        fun deleteCurrentContext(project: Project) {
            val context = getCurrentContext(project)
            val cluster = getContextCluster(project, context)
            val user = getContextUser(project, context)

            project.logger.info("Delete current context {} with related cluster {} and user {} information", context, cluster, user)
            ProcessUtil.executeCommand(project,
                    "kubectl config delete-context $context", throwErrorOnFailure = false)
            ProcessUtil.executeCommand(project,
                    "kubectl config delete-user $user", throwErrorOnFailure = false)
            ProcessUtil.executeCommand(project,
                    "kubectl config delete-cluster $cluster", throwErrorOnFailure = false)
        }

        fun deleteAllPvcs(project: Project) {
            ProcessUtil.executeCommand(project,
                    "kubectl delete pvc --all", throwErrorOnFailure = false)
        }

        fun getIngresHost(project: Project, ingressName: String): String {
            return getWithPath(project, "ing $ingressName", "{.items[*].spec.rules[*].host}")
        }

        fun getServiceExternalIp(project: Project, serviceName: String): String {
            return getWithPath(project, "service $serviceName", "{.status.loadBalancer.ingress[*].ip}")
        }

        private fun getCurrentContext(project: Project): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config current-context", logOutput = false)
        }

        private fun getContextCluster(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.cluster}' --raw", logOutput = false)
        }

        private fun getContextUser(project: Project, contextName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.user}' --raw", logOutput = false)
        }

        private fun getClusterServer(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.server}' --raw", logOutput = false)
        }

        private fun getClusterCertificateAuthorityData(project: Project, clusterName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}' --raw", logOutput = false)
        }

        private fun getUserClientKeyData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-key-data}' --raw", logOutput = false)
        }

        private fun getUserClientCertificateData(project: Project, userName: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-certificate-data}' --raw", logOutput = false)
        }

        private fun getNameAndGrep(project: Project, command: String, grepFor: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl get $command -o name | grep $grepFor", throwErrorOnFailure = false, logOutput = false)
        }

        private fun getWithPath(project: Project, command: String, jsonpath: String): String {
            return ProcessUtil.executeCommand(project,
                    "kubectl get $command -o \"jsonpath=$jsonpath\"")
        }
    }
}
