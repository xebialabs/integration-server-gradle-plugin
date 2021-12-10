package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.AwsOpenshiftProvider
import ai.digital.integration.server.common.util.HtmlUtil
import ai.digital.integration.server.common.util.KubeCtlHelper
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File
import java.util.*

@Suppress("UnstableApiUsage")
open class AwsOpenshiftHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        createOcContext()

        updateControllerManager()
        updateOperatorApplications()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateOperatorCrValues()

        updateInfrastructure(getApiServerUrl(), getOcApiServerToken())

        applyYamlFiles()

        ocLogin()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        waitForBoot()
    }

    private fun exec(command: String): String {
        val workDir = File(getProviderHomeDir())
        return ProcessUtil.executeCommand(command, workDir)
    }

    private fun ocLogin() {
        exec("oc login ${getApiServerUrl()} --username ${getOcLogin()} --password \"${getOcPassword()}\"")
    }

    private fun ocLogout() {
        try {
            exec("oc logout")
        } catch (e: Exception) {
            // ignore, if throws exception, it only means that already loged out, safe to ignore.
        }
    }

    private fun getOcApiServerToken(): String {
        val basicAuthToken = Base64.getEncoder().encodeToString("${getOcLogin()}:${getOcPassword()}".toByteArray())
        val oauthHostName = getProvider().oauthHostName.get()

        ocLogout()

        val command1Output =
            exec("curl -vvv -L -k -c cookie -b cookie  -H \"Authorization: Basic $basicAuthToken\" https://$oauthHostName/oauth/token/request")
        val doc1 = HtmlUtil.htmlToDocument(command1Output)

        val code = doc1.select("form input[name=\"code\"]").`val`()
        val csrf = doc1.select("form input[name=\"csrf\"]").`val`()

        val command2Output =
            exec("curl -vvv -L -k -c cookie -b cookie -d 'code=$code&csrf=$csrf' -H \"Authorization: Basic $basicAuthToken\" https://$oauthHostName/oauth/token/display")
        val doc2 = HtmlUtil.htmlToDocument(command2Output)
        return doc2.select("code").text()
    }

    fun shutdownCluster() {
        ocLogin()

        project.logger.lifecycle("Undeploy operator")
        undeployCis()

        project.logger.lifecycle("Delete all PVCs")
        getKubectlHelper().deleteAllPVCs()

        ocLogout()
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-openshift"
    }

    override fun getProvider(): AwsOpenshiftProvider {
        return getProfile().awsOpenshift
    }

    override fun getOperatorImage(): String {
        return getProvider().operatorImage.value("xebialabs/deploy-operator:1.2.0-openshift").get()
    }

    override fun getStorageClass(): String {
        return getProvider().storageClass.value("aws-efs").get()
    }

    private fun updateInfrastructure(apiServerURL: String, token: String) {
        project.logger.lifecycle("Updating operator's infrastructure")

        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec[0].children[0].serverUrl" to apiServerURL,
            "spec[0].children[0].openshiftToken" to token
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, true)

    override fun hasIngress(): Boolean = false

    override fun getWorkerPodName(position: Int) = "pod/dai-ocp-xld-digitalai-deploy-ocp-worker-$position"

    override fun getMasterPodName(position: Int) = "pod/dai-ocp-xld-digitalai-deploy-ocp-master-$position"

    private fun getApiServerUrl() = getProvider().apiServerURL.get()

    private fun getOcLogin() = project.property("ocLogin")

    private fun getOcPassword() = project.property("ocPassword")

    private fun createOcContext() {
        project.logger.lifecycle("Updating kube config for Open Shift")
        exec("export KUBECONFIG=~/.kube/config")
        ocLogin()
    }
}
