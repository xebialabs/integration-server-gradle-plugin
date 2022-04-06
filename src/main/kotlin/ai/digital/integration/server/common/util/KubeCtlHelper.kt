package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.apache.commons.codec.binary.Base64
import org.gradle.api.Project
import java.io.File
import java.nio.charset.StandardCharsets

open class KubeCtlHelper(val project: Project, val namespace: String?, isOpenShift: Boolean = false) {

    val command = if (isOpenShift) "oc" else "kubectl"

    fun applyFile(file: File) {
        ProcessUtil.executeCommand(project,
            namespaceWrapper("$command apply -f \"${file.absolutePath}\""))
    }

    fun deleteFile(file: File) {
        ProcessUtil.executeCommand(project, namespaceWrapper("$command delete -f \"${file.absolutePath}\""))
    }

    fun wait(resource: String, condition: String, timeoutSeconds: Int): Boolean {
        project.logger.lifecycle("Waiting for resource $resource to be $condition")
        val expectedEndTime = System.currentTimeMillis() + timeoutSeconds * 1000
        while (expectedEndTime > System.currentTimeMillis()) {
            val result = ProcessUtil.executeCommand(project,
                namespaceWrapper("$command wait --for condition=$condition --timeout=${timeoutSeconds}s $resource"),
                    throwErrorOnFailure = false)
            if (result.contains("condition met")) {
                return true
            }
            Thread.sleep(1000)
        }
        return false
    }

    fun savePodLogs(podName: String) {
        val name = if (podName.startsWith("pod/")) podName.substring(4) else podName
        try {
            val logContent = ProcessUtil.executeCommand(project,
                    command = namespaceWrapper("$command logs $name"),
                    logOutput = false)
            val logDir = DeployServerUtil.getLogDir(project)
            File(logDir, "$name.log").writeText(logContent, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // ignore, if throws exception, it means that pod is still waiting to start
        }
    }

    fun setDefaultStorageClass(newDefaultStorageClass: String) {
        ProcessUtil.executeCommand(project,
            namespaceWrapper("$command get sc -o name") +
                        "|sed -e 's/.*\\///g' " +
                        "|xargs -I {} " +
                    namespaceWrapper("$command patch storageclass {} -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"false\"}}}'"))
        ProcessUtil.executeCommand(project,
            namespaceWrapper("$command patch storageclass $newDefaultStorageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"true\"}}}'"))
    }

    fun hasStorageClass(storageClass: String): Boolean {
        val result = getNameAndGrep("storageclass", storageClass)
        return result.contains(storageClass)
    }

    fun getCurrentContextInfo(skip: Boolean = false): InfrastructureInfo {
        val context = getCurrentContext()
        val cluster = getContextCluster(context)
        val user = getContextUser(context)
        return InfrastructureInfo(
                cluster,
                user,
                getClusterServer(cluster),
                getClusterCertificateAuthorityData(cluster),
                if (!skip) getUserClientCertificateData(user) else null,
                if (!skip) getUserClientKeyData(user) else null
        )
    }

    fun deleteCurrentContext() {
        try {
            val context = getCurrentContext()
            val cluster = getContextCluster(context)
            val user = getContextUser(context)

            project.logger.info("Current cluster context is being deleted {} with related cluster {} and user {} information",
                    context,
                    cluster,
                    user)

            fun delete(what: String) {
                ProcessUtil.executeCommand(project, "$command config $what", throwErrorOnFailure = false)
            }

            delete("delete-context $context")
            delete("delete-user $user")
            delete("delete-cluster $cluster")
        } catch (e: RuntimeException) {
            project.logger.info("Skipping delete of current context because: {}", e.message)
        }
    }

    fun deleteAllPVCs() {
        ProcessUtil.executeCommand(project,
            namespaceWrapper("$command delete pvc --all --grace-period=1"), throwErrorOnFailure = false)
    }

    fun getIngresHost(ingressName: String): String {
        return getWithPath("ing $ingressName", "{.items[*].spec.rules[*].host}")
    }

    fun getServiceExternalIp(serviceName: String): String {
        return getWithPath("get $serviceName", "{.status.loadBalancer.ingress[*].ip}")
    }

    fun getCurrentContext(): String {
        return ProcessUtil.executeCommand(project,
            "$command config current-context", logOutput = false)
    }

    private fun configView(jsonPath: String) = ProcessUtil.executeCommand(project,
        "$command config view -o jsonpath='$jsonPath' --raw", logOutput = false)

    fun getContextCluster(contextName: String) =
            configView("{.contexts[?(@.name == \"$contextName\")].context.cluster}")

    fun getContextUser(contextName: String) =
            configView("{.contexts[?(@.name == \"$contextName\")].context.user}")

    fun getClusterServer(clusterName: String) =
            configView("{.clusters[?(@.name == \"$clusterName\")].cluster.server}")

    private fun configView(jsonPath: String, fallbackJsonPath: String): String {
        val data = configView(jsonPath)
        return data.ifEmpty {
            val path = configView(fallbackJsonPath)
            Base64.encodeBase64String(File(path).readText().toByteArray())
        }
    }

    fun getClusterCertificateAuthorityData(clusterName: String): String =
            configView("{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority-data}",
                    "{.clusters[?(@.name == \"$clusterName\")].cluster.certificate-authority}")


    private fun getUserClientKeyData(userName: String): String =
            configView("{.users[?(@.name == \"$userName\")].user.client-key-data}",
                    "{.users[?(@.name == \"$userName\")].user.client-key}")


    private fun getUserClientCertificateData(userName: String): String =
            configView("{.users[?(@.name == \"$userName\")].user.client-certificate-data}",
                    "{.users[?(@.name == \"$userName\")].user.client-certificate}")

    private fun getNameAndGrep(params: String, grepFor: String): String {
        return ProcessUtil.executeCommand(project,
            namespaceWrapper("$command get $params -o name | grep $grepFor"), throwErrorOnFailure = false, logOutput = false)
    }

    private fun getWithPath(subCommand: String, jsonpath: String): String {
        return ProcessUtil.executeCommand(project,
            namespaceWrapper("$command $subCommand -o 'jsonpath=$jsonpath'"))
    }

    fun getCrd(groupName: String): String {
        return getWithPath("get crd", "{.items[?(@..spec.group == \"$groupName\")].metadata.name}")
    }

    fun getCr(crdName: String): String {
        return getWithPath("get $crdName", "{.items[0].metadata.name}")
    }

    fun getResourceNames(resource: String, productName: ProductName): String {
        return ProcessUtil.executeCommand(project,
            namespaceWrapper("$command get $resource -o name") + " | grep ${productName.shortName} | tr \"\\n\" \" \" | sed -e 's/,\$//'",
            logOutput = false, throwErrorOnFailure = false)
    }

    fun getResourceNames(resource: String): String {
        return ProcessUtil.executeCommand(project,
            namespaceWrapper("$command get $resource -o name"), logOutput = false, throwErrorOnFailure = false)
    }

    fun deleteNames(names: String): String {
        return ProcessUtil.executeCommand(project,
            namespaceWrapper("$command delete $names"), logOutput = false, throwErrorOnFailure = false)
    }

    fun clearCrFinalizers(names: String): String {
        return ProcessUtil.executeCommand(project,
            namespaceWrapper("$command patch $names -p '{\"metadata\":{\"finalizers\":[]}}' --type=merge"), logOutput = false, throwErrorOnFailure = false)
    }

    private fun namespaceWrapper(kcCommand: String): String {
        return namespace?.let { "$kcCommand --namespace $namespace" } ?: kcCommand
    }
}
