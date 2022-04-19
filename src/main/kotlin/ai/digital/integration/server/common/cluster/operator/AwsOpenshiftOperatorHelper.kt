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

    fun updateOperator() {
        cleanUpCluster(getProvider().cleanUpWaitTimeout.get())
        updateInfrastructure(AwsOpenshiftHelper(project, productName).getApiServerUrl(), getOcApiServerToken())
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
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        waitForBoot()
        turnOffLogging()
    }

    fun shutdownCluster() {
        AwsOpenshiftHelper(project, productName).ocLogin()
        undeployCluster()
        AwsOpenshiftHelper(project, productName).ocLogout()
    }

    fun getOcApiServerToken(): String {
        val basicAuthToken = Base64.getEncoder().encodeToString("${AwsOpenshiftHelper(project, productName).getOcLogin()}:${AwsOpenshiftHelper(project, productName).getOcPassword()}".toByteArray())
        val oauthHostName = getProvider().oauthHostName.get()

        AwsOpenshiftHelper(project, productName).ocLogout()

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
        return AwsOpenshiftHelper(project, productName).getStorageClass()
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

    override fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, getNamespace(), true)

    override fun hasIngress(): Boolean = false

    override fun getWorkerPodName(position: Int) =
        "pod/dai-ocp-${getPrefixName()}-digitalai-${getName()}-ocp-worker-$position"

    override fun getMasterPodName(position: Int) =
        "pod/dai-ocp-${getPrefixName()}-digitalai-${getName()}-ocp-${getMasterPodNameSuffix(position)}"

    override fun getPostgresPodName(position: Int) = "pod/dai-ocp-${getPrefixName()}-postgresql-$position"

    override fun getRabbitMqPodName(position: Int) = "pod/dai-ocp-${getPrefixName()}-rabbitmq-$position"

    override fun getProviderCrContextPath(): String = "spec.route.path"

}