package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.AwsEksHelper
import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.common.cluster.operator.GcpGkeHelper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import ai.digital.integration.server.common.util.XlCliUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

open class OperatorBasedUpgradeDeployClusterTask  : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedUpgradeDeployCluster"
    }

    @Input
    val imageRepositoryName = project.objects.property<String>()

    @Input
    val imageTargetVersion = project.objects.property<String>()

    @Input
    val operatorBranch = project.objects.property<String>()

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)

        val operatorZip = operatorBranchToOperatorZip(operatorHelper)
        val answersFile = prepareAnswersFile(operatorHelper, operatorZip)
        opUsingAnswersFile(operatorHelper, answersFile)

        operatorHelper.waitForDeployment()
        operatorHelper.waitForMasterPods()
        operatorHelper.waitForWorkerPods()
        operatorHelper.waitForBoot()
    }

    private fun prepareAnswersFile(operatorHelper: OperatorHelper, operatorZip: Path?): File {

        val operatorImage = operatorHelper.getOperatorImage()
        val crdName = operatorHelper.getKubectlHelper().getCrd()
        val crName = operatorHelper.getKubectlHelper().getCr(crdName)
        val k8sSetup = XlCliUtil.XL_OP_MAPPING[DeployClusterUtil.getOperatorProviderName(project)]!!

        val answersFile = if (k8sSetup == "GoogleGKE") {
            operatorHelper.getTemplate("operator/xl-upgrade/answers_gke.yaml", "answers.yaml")
        } else if (k8sSetup == "Openshift") {
            operatorHelper.getTemplate("operator/xl-upgrade/answers_aws_openshift.yaml", "answers.yaml")
        } else if (k8sSetup == "AwsEKS") {
            operatorHelper.getTemplate("operator/xl-upgrade/answers_eks.yaml", "answers.yaml")
        } else {
            operatorHelper.getTemplate("operator/xl-upgrade/answers.yaml")
        }
        project.logger.lifecycle("Preparing answers file ${answersFile.absolutePath}")

        val kubeContextInfo = operatorHelper.getCurrentContextInfo()

        val answersFileTemplateTmp = answersFile.readText(Charsets.UTF_8)
                .replace("{{CRD_NAME}}", crdName)
                .replace("{{CR_NAME}}", crName)
                .replace("{{K8S_API_SERVER_URL}}", kubeContextInfo.apiServerURL!!)
                .replace("{{K8S_SETUP}}", k8sSetup)
                .replace("{{OPERATOR_IMAGE}}", operatorImage)
                .replace("{{REPOSITORY_NAME}}", imageRepositoryName.get())
                .replace("{{IMAGE_TAG}}", imageTargetVersion.get())
                .replace("{{OPERATOR_ZIP_DEPLOY}}", operatorZip?.toAbsolutePath().toString())

        val answersFileTemplate = if (k8sSetup == "GoogleGKE") {
            answersFileTemplateTmp
                    .replace("{{K8S_TOKEN}}", (operatorHelper as GcpGkeHelper).getAccessToken())
        } else if (k8sSetup == "Openshift") {
            answersFileTemplateTmp
                    .replace("{{K8S_TOKEN}}", (operatorHelper as AwsOpenshiftHelper).getOcApiServerToken())
        } else if (k8sSetup == "AwsEKS") {
            val awsEksHelper = operatorHelper as AwsEksHelper
            answersFileTemplateTmp
                    .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                    .replace("{{CLUSTER_NAME}}", awsEksHelper.getProvider().clusterName.get())
                    .replace("{{AWS_ACCESS_KEY}}", awsEksHelper.getProvider().getAwsAccessKey())
                    .replace("{{AWS_ACCESS_SECRET}}", awsEksHelper.getProvider().getAwsSecretKey())
        } else {
            answersFileTemplateTmp
                    .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                    .replace("{{K8S_CLIENT_KEY}}", kubeContextInfo.tlsPrivateKey!!)
        }

        answersFile.writeText(answersFileTemplate)
        return answersFile
    }

    private fun opUsingAnswersFile(operatorHelper: OperatorHelper, answersFile: File) {
        project.logger.lifecycle("Applying prepared answers file ${answersFile.absolutePath}")
        XlCliUtil.xlOp(project, answersFile, operatorHelper.getProfile().xlCliVersion.get(), File(operatorHelper.getProviderHomeDir()),
                operatorHelper.getProvider().blueprintPath.orNull)
    }

    private fun operatorBranchToOperatorZip(operatorHelper: OperatorHelper): Path? {
        operatorBranch.orNull.let { branch ->
            val operatorPath = GitUtil.checkout("xl-deploy-kubernetes-operator", Paths.get(operatorHelper.getProviderHomeDir()), branch)
            val src = Paths.get(operatorPath.toAbsolutePath().toString(), operatorHelper.getProviderHomePath())
            val dest = Paths.get(operatorHelper.getProviderHomeDir(), "operator-upgrade.zip")
            ant.withGroovyBuilder { "zip"("basedir" to src.toAbsolutePath().toString(), "destfile" to dest.toAbsolutePath().toString()) }
            return dest
        }
    }
}
