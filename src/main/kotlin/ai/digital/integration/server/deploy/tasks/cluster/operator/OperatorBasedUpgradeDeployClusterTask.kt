package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.XlCliUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.internals.cluster.operator.OperatorHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File

open class OperatorBasedUpgradeDeployClusterTask  : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedUpgradeDeployCluster"
    }

    @Input
    val repositoryName = project.objects.property<String>().value("xebialabs")

    @Input
    val targetVersion = project.objects.property<String>()

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project)

        val answersFile = prepareAnswersFile(operatorHelper)
        opUsingAnswersFile(operatorHelper, answersFile)

        operatorHelper.waitForDeployment()
        operatorHelper.waitForMasterPods()
        operatorHelper.waitForWorkerPods()
        operatorHelper.waitForBoot()
    }

    private fun prepareAnswersFile(operatorHelper: OperatorHelper): File {
        val answersFile = operatorHelper.getTemplate("operator/xl-upgrade/answers.yaml")
        project.logger.lifecycle("Preparing answers file ${answersFile.absolutePath}")

        val operatorImage = operatorHelper.getOperatorImage()
        val crdName = operatorHelper.getKubectlHelper().getCrd()
        val crName = operatorHelper.getKubectlHelper().getCr(crdName)
        val k8sSetup = XlCliUtil.XL_OP_MAPPING[DeployClusterUtil.getOperatorProviderName(project)]

        val kubeContextInfo = operatorHelper.getCurrentContextInfo()

        val answersFileTemplate = answersFile.readText(Charsets.UTF_8)
                .replace("{{CRD_NAME}}", crdName)
                .replace("{{CR_NAME}}", crName)
                .replace("{{K8S_API_SERVER_URL}}", kubeContextInfo.apiServerURL!!)
                .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                .replace("{{K8S_CLIENT_KEY}}", kubeContextInfo.tlsPrivateKey!!)
                .replace("{{K8S_SETUP}}", k8sSetup!!)
                .replace("{{OPERATOR_IMAGE}}", operatorImage)
                .replace("{{REPOSITORY_NAME}}", repositoryName.get())
                .replace("{{IMAGE_TAG}}", targetVersion.get())
        answersFile.writeText(answersFileTemplate)
        return answersFile
    }

    private fun opUsingAnswersFile(operatorHelper: OperatorHelper, answersFile: File) {
        project.logger.lifecycle("Applying prepared answers file ${answersFile.absolutePath}")
        XlCliUtil.xlOp(project, answersFile, operatorHelper.getProfile().xlCliVersion.get(), File(operatorHelper.getProviderHomeDir()))
    }
}
