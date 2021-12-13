package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.InfrastructureInfo
import org.gradle.api.Project
import java.io.File

open class KubeCtlHelper(val project: Project, isOpenShift: Boolean = false) {

    val command = if (isOpenShift) "oc" else "kubectl"

    fun applyFile(file: File) {
        ProcessUtil.executeCommand(project,
            "$command apply -f \"${file.absolutePath}\"")
    }

    fun deleteFile(file: File) {
        ProcessUtil.executeCommand(project, "$command delete -f \"${file.absolutePath}\"")
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
        fun patch(storageClass: String, isDefault: Boolean) {
            ProcessUtil.executeCommand(project,
                " $command patch storageclass $storageClass -p '{\"metadata\": {\"annotations\":{\"storageclass.kubernetes.io/is-default-class\":\"$isDefault\"}}}'")
        }

        patch(newDefaultStorageClass, true)
        patch(oldDefaultStorageClass, false)
    }

    fun hasStorageClass(storageClass: String): Boolean {
        val result = getNameAndGrep("storageclass", storageClass)
        return result.contains(storageClass)
    }

    fun getCurrentContextInfo(skip:Boolean = false): InfrastructureInfo {
        val context = getCurrentContext()
        val cluster = getContextCluster(context)
        val user = getContextUser(context)
        val info = InfrastructureInfo(
            cluster,
            user,
            getClusterServer(cluster),
            getClusterCertificateAuthorityData(cluster),
            if(!skip) getUserClientCertificateData(user) else null,
            if(!skip) getUserClientKeyData(user) else null
        )
        project.logger.lifecycle("kubeContextInfo {}", info)
        return info
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
            "$command delete pvc --all", throwErrorOnFailure = false)
    }

    private fun getCurrentContext(): String {
        return ProcessUtil.executeCommand(project,
            "$command config current-context", logOutput = false)
    }

    private fun configView(jsonPath: String) = ProcessUtil.executeCommand(project,
        "$command config view -o jsonpath='$jsonPath' --raw", logOutput = false)

    private fun getContextCluster(contextName: String) =
        configView("{.contexts[?(@.name == \"$contextName\")].context.cluster}")

    private fun getContextUser(contextName: String) =
        configView("{.contexts[?(@.name == \"$contextName\")].context.user}")

    private fun getClusterServer(clusterName: String) =
        configView("{.clusters[?(@.name == \"$clusterName\")].cluster.server}")

    private fun configView(jsonPath: String, fallbackJsonPath: String): String {
        val data = configView(jsonPath)
        return if (data == "") {
            val path = configView(fallbackJsonPath)
            File(path).readText()
        } else {
            data
        }
    }

    private fun getClusterCertificateAuthorityData(clusterName: String): String =
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
            "$command get $params -o name | grep $grepFor", throwErrorOnFailure = false, logOutput = false)
    }

    private fun getWithPath(command: String, jsonpath: String): String {
        return ProcessUtil.executeCommand(project,
            "$command get $command -o \"jsonpath=$jsonpath\"")
    }
}
