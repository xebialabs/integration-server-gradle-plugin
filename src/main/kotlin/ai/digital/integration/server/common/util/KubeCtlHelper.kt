package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.InfrastructureInfo
import org.gradle.api.Project
import java.io.File

open class KubeCtlHelper(val project: Project, val isOpenShift: Boolean = false) {

    val command = if (isOpenShift) "oc" else "kubectl"

    fun applyFile(file: File) {
        ProcessUtil.executeCommand(project,
            "$command apply -f ${file.absolutePath}")
    }

    fun wait(resource: String, condition: String, timeoutSeconds: Int): Boolean {
        project.logger.lifecycle("Waiting for resource $resource to be $condition")
        val expectedEndTime = System.currentTimeMillis() + timeoutSeconds * 1000
        while (expectedEndTime > System.currentTimeMillis()) {
            val result = ProcessUtil.executeCommand(project,
                "$command wait --for condition=$condition --timeout=${timeoutSeconds}s $resource",
                throwErrorOnFailure = false)
            if (result.contains("condition met")) {
                return true
            }
            Thread.sleep(1000)
        }
        return false
    }

    fun setDefaultStorageClass(oldDefaultStorageClass: String, newDefaultStorageClass: String) {
        ProcessUtil.executeCommand(project,
            " $command patch storageclass $newDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"true\"}}}'")
        ProcessUtil.executeCommand(project,
            " $command patch storageclass $oldDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"false\"}}}'")
    }

    fun hasStorageClass(storageClass: String): Boolean {
        val result = getNameAndGrep("storageclass", storageClass)
        return result.contains(storageClass)
    }

    fun getCurrentContextInfo(): InfrastructureInfo {
        val context = getCurrentContext()
        val cluster = getContextCluster(context)
        val user = getContextUser(context)
        return InfrastructureInfo(
            cluster,
            user,
            getClusterServer(cluster),
            getClusterCertificateAuthorityData(cluster),
            getUserClientCertificateData(user),
            getUserClientKeyData(user)
        )
    }

    fun deleteCurrentContext() {
        val context = getCurrentContext()
        val cluster = getContextCluster(context)
        val user = getContextUser(context)

        project.logger.info("Delete current context {} with related cluster {} and user {} information",
            context,
            cluster,
            user)
        ProcessUtil.executeCommand(project,
            "$command config delete-context $context", throwErrorOnFailure = false)
        ProcessUtil.executeCommand(project,
            "$command config delete-user $user", throwErrorOnFailure = false)
        ProcessUtil.executeCommand(project,
            "$command config delete-cluster $cluster", throwErrorOnFailure = false)
    }

    fun deleteAllPvcs() {
        ProcessUtil.executeCommand(project,
            "$command delete pvc --all", throwErrorOnFailure = false)
    }

    fun getIngresHost(ingressName: String): String {
        return getWithPath("ing $ingressName", "{.items[*].spec.rules[*].host}")
    }

    fun getServiceExternalIp(serviceName: String): String {
        return getWithPath("service $serviceName", "{.status.loadBalancer.ingress[*].ip}")
    }

    private fun getCurrentContext(): String {
        return ProcessUtil.executeCommand(project,
            "$command config current-context", logOutput = false)
    }

    private fun getContextCluster(contextName: String): String {
        return ProcessUtil.executeCommand(project,
            "$command config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.cluster}' --raw",
            logOutput = false)
    }

    private fun getContextUser(contextName: String): String {
        return ProcessUtil.executeCommand(project,
            "$command config view -o jsonpath='{.contexts[?(@.name == \"$contextName\")].context.user}' --raw",
            logOutput = false)
    }

    private fun getClusterServer(clusterName: String): String {
        return ProcessUtil.executeCommand(project,
            "$command config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.server}' --raw",
            logOutput = false)
    }

    private fun getClusterCertificateAuthorityData(clusterName: String): String {
        return ProcessUtil.executeCommand(project,
            "$command config view -o jsonpath='{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}' --raw",
            logOutput = false)
    }

    private fun getUserClientKeyData(userName: String): String {
        return ProcessUtil.executeCommand(project,
            "$command config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-key-data}' --raw",
            logOutput = false)
    }

    private fun getUserClientCertificateData(userName: String): String {
        return ProcessUtil.executeCommand(project,
            "$command config view -o jsonpath='{.users[?(@.name == \"$userName\")].user.client-certificate-data}' --raw",
            logOutput = false)
    }

    private fun getNameAndGrep(params: String, grepFor: String): String {
        return ProcessUtil.executeCommand(project,
            "$command get $params -o name | grep $grepFor", throwErrorOnFailure = false, logOutput = false)
    }

    private fun getWithPath(command: String, jsonpath: String): String {
        return ProcessUtil.executeCommand(project,
            "$command get $command -o \"jsonpath=$jsonpath\"")
    }
}
