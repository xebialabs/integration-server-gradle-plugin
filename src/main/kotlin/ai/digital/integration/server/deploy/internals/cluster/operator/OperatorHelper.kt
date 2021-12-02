package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.operator.Provider
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

const val OPERATOR_FOLDER_NAME: String = "xl-deploy-kubernetes-operator"

const val CR_REL_PATH = "digitalai-deploy/kubernetes/daideploy_cr.yaml"

const val CONTROLLER_MANAGER_REL_PATH = "digitalai-deploy/kubernetes/template/deployment.yaml"

const val OPERATOR_APPS_REL_PATH = "digitalai-deploy/applications.yaml"

const val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-deploy/infrastructure.yaml"

const val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-deploy/deployment-cr.yaml"

const val OPERATOR_PACKAGE_REL_PATH = "digitalai-deploy/deployment.yaml"

const val XL_DIGITAL_AI_PATH = "digital-ai.yaml "

@Suppress("UnstableApiUsage")
abstract class OperatorHelper(val project: Project) {
    fun getOperatorHomeDir(): String =
            project.buildDir.toPath().resolve(OPERATOR_FOLDER_NAME).toAbsolutePath().toString()

    fun getProviderWorkDir(): String =
        project.buildDir.toPath().resolve("${getProvider().name.get()}-work").toAbsolutePath().toString()

    fun getProfile(): OperatorProfile {
        return DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
    }

    fun updateControllerManager() {
        val file = File(getProviderHomeDir(), CONTROLLER_MANAGER_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec.template.spec.containers[1].image" to getOperatorImage()
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorApplications() {
        val file = File(getProviderHomeDir(), OPERATOR_APPS_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].name" to getProvider().operatorPackageVersion
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeployment() {
        val file = File(getProviderHomeDir(), OPERATOR_PACKAGE_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec.package" to "Applications/xld-operator-app/${getProvider().operatorPackageVersion}"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeploymentCr() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_PACKAGE_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec.package" to "Applications/xld-cr/${getProvider().operatorPackageVersion}"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun waitForDeployment() {
        val resources = arrayOf(
                "deployment.apps/xld-operator-controller-manager",
                "deployment.apps/digitalaideploy-sample-nginx-ingress-controller",
                "deployment.apps/digitalaideploy-sample-nginx-ingress-controller-default-backend"
        )
        resources.forEach { resource ->
            KubeCtlUtil.wait(project, resource, "Available", getProfile().deploymentTimeoutSeconds.get())
        }
    }

    fun waitForMasterPods() {
        val servers = DeployServerUtil.getServers(project)
        val resources = List(servers.size) { position ->
            "pod/digitalaideploy-sample-digitalai-deploy-master-$position"
        }

        resources.forEach { resource ->
            KubeCtlUtil.wait(project, resource, "Ready", getProfile().deploymentTimeoutSeconds.get())
        }
    }

    fun waitForWorkerPods() {
        val workers = DeployServerUtil.getServers(project)
        val resources = List(workers.size) { position ->
            "pod/digitalaideploy-sample-digitalai-deploy-worker-$position"
        }
        resources.forEach { resource ->
            KubeCtlUtil.wait(project, resource, "Ready", getProfile().deploymentTimeoutSeconds.get())
        }
    }

    fun waitForBoot(host: String) {
        val url ="http://$host/xl-deploy/deployit/metadata/type"
        val server = DeployServerUtil.getServer(project)
        WaitForBootUtil.byPort(project, "Deploy", url, null, server.pingRetrySleepTime, server.pingTotalTries)
    }

    fun undeployCis() {
        val fileStream = {}::class.java.classLoader.getResourceAsStream("operator/python/undeploy.py")
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), "undeploy.py")
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        CliUtil.executeScripts(project, listOf(resultComposeFilePath.toFile()), "undeploy.py", false, 4516)
    }

    open fun getOperatorImage(): String {
        return getProvider().operatorImage.value("xebialabs/deploy-operator:1.2.0").get()
    }

    open fun applyDigitalAi() {
        val xlDigitalAiPath = File(getProviderHomeDir(), XL_DIGITAL_AI_PATH)
        project.logger.lifecycle("Applying Digital AI Deploy platform on cluster ($xlDigitalAiPath)")
        XlCliUtil.apply(project, xlDigitalAiPath)
    }

    abstract fun updateInfrastructure(infraInfo: InfrastructureInfo)

    abstract fun getProviderHomeDir(): String

    abstract fun getProvider(): Provider
}
