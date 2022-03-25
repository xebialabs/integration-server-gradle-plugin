package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsOpenshiftProvider
import ai.digital.integration.server.common.util.HtmlUtil
import ai.digital.integration.server.common.util.KubeCtlHelper
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File
import java.util.*

@Suppress("UnstableApiUsage")
open class AwsOpenshiftHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    fun launchCluster() {
        createOcContext()

        updateOperatorApplications()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateDeploymentValues()
        updateOperatorCrValues()

        updateInfrastructure(getApiServerUrl(), getOcApiServerToken())

        ocLogin()
        cleanUpCluster(getProvider().cleanUpWaitTimeout.get())
    }

    fun installCluster() {
        applyYamlFiles()

        turnOnLogging()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        waitForBoot()
        turnOffLogging()
    }

    private fun exec(command: String): String {
        val workDir = File(getProviderHomeDir())
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
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

    fun getOcApiServerToken(): String {
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
        undeployCluster()
        ocLogout()
    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-openshift"
    }

    override fun getProvider(): AwsOpenshiftProvider {
        return getProfile().awsOpenshift
    }

    override fun getStorageClass(): String {
        return getProvider().storageClass.getOrElse("aws-efs")
    }

    private fun updateInfrastructure(apiServerURL: String, token: String) {
        project.logger.lifecycle("Updating operator's infrastructure")

        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>("spec[0].children[0].serverUrl" to apiServerURL,
            "spec[0].children[0].openshiftToken" to token)
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun updateCustomOperatorCrValues(crValuesFile: File) {
        val pairs: MutableMap<String, Any> =
            mutableMapOf("spec.postgresql.postgresqlExtendedConf.listenAddresses" to "*")
        YamlFileUtil.overlayFile(crValuesFile, pairs, minimizeQuotes = false)
    }

    override fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, true)

    override fun hasIngress(): Boolean = false

    override fun getWorkerPodName(position: Int) =
        "pod/dai-ocp-${getPrefixName()}-digitalai-${getName()}-ocp-worker-$position"

    override fun getMasterPodName(position: Int) =
        "pod/dai-ocp-${getPrefixName()}-digitalai-${getName()}-ocp-${getMasterPodNameSuffix(position)}"

    override fun getPostgresPodName(position: Int) = "pod/dai-ocp-${getPrefixName()}-postgresql-$position"

    override fun getRabbitMqPodName(position: Int) = "pod/dai-ocp-${getPrefixName()}-rabbitmq-$position"

    override fun getProviderCrContextPath(): String = "spec.route.path"

    private fun getApiServerUrl() = getProvider().apiServerURL.get()

    private fun getOcLogin() = project.property("ocLogin")

    private fun getOcPassword() = project.property("ocPassword")

    private fun createOcContext() {
        project.logger.lifecycle("Updating kube config for Open Shift")
        exec("export KUBECONFIG=~/.kube/config")
        ocLogin()
    }
}
