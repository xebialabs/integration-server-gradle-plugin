package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.setup.AwsOpenshiftHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsOpenshiftProvider
import ai.digital.integration.server.common.util.HtmlUtil
import ai.digital.integration.server.common.util.KubeCtlHelper
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File
import java.util.*

@Suppress("UnstableApiUsage")
open class AwsOpenshiftOperatorHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    private val awsOpenshiftHelper : AwsOpenshiftHelper = AwsOpenshiftHelper(project, productName, getProfile())

    fun launchCluster(){
        awsOpenshiftHelper.launchCluster()
    }

    fun updateOperator() {
        cleanUpCluster(getProvider().cleanUpWaitTimeout.get())
        updateInfrastructure(awsOpenshiftHelper.getApiServerUrl(), getOcApiServerToken())
        updateOperatorApplications()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateOperatorEnvironment()
        updateDeploymentValues()
        updateOperatorCrValues()
    }

    fun installCluster() {
        applyYamlFiles()

        turnOnLogging()
        val namespaceAsPrefix = getNamespace()?.let { "$it-" } ?: ""
        waitForDeployment(getProfile().ingressType.get(), getProfile().deploymentTimeoutSeconds.get(), namespaceAsPrefix)
        waitForMasterPods(getProfile().deploymentTimeoutSeconds.get())
        waitForWorkerPods(getProfile().deploymentTimeoutSeconds.get())

        createClusterMetadata()
        waitForBoot(getContextRoot(), getFqdn())
        turnOffLogging()
    }

    fun shutdownCluster() {
        awsOpenshiftHelper.ocLogin()
        undeployCluster()
        awsOpenshiftHelper.ocLogout()
    }

    fun getOcApiServerToken(): String {
        val basicAuthToken = Base64.getEncoder().encodeToString("${awsOpenshiftHelper.getOcLogin()}:${awsOpenshiftHelper.getOcPassword()}".toByteArray())
        val oauthHostName = getProvider().oauthHostName.get()

        awsOpenshiftHelper.ocLogout()

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

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-openshift"
    }

    override fun getProvider(): AwsOpenshiftProvider {
        return getProfile().awsOpenshift
    }

    override fun getStorageClass(): String {
        return awsOpenshiftHelper.getStorageClass()
    }

    private fun updateInfrastructure(apiServerURL: String, token: String) {
        project.logger.lifecycle("Updating operator's infrastructure")
        super.updateInfrastructure()

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

    override fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, getNamespace(), true)

    override fun hasIngress(): Boolean = false

    override fun getCrName(): String {
        val operatorNamespace = getNamespace()?.let { "-$it" } ?: ""
        return "dai-ocp-${getPrefixName()}$operatorNamespace"
    }

    override fun getWorkerPodName(position: Int) =
        "pod/${getCrName()}-digitalai-${getName()}-ocp-worker-$position"

    override fun getMasterPodName(position: Int) =
        "pod/${getCrName()}-digitalai-${getName()}-ocp-${getMasterPodNameSuffix(position)}"

    override fun getPostgresPodName(position: Int) = "pod/${getCrName()}-postgresql-$position"

    override fun getRabbitMqPodName(position: Int) = "pod/${getCrName()}-rabbitmq-$position"

    override fun getProviderCrContextPath(): String = "spec.route.path"

}
