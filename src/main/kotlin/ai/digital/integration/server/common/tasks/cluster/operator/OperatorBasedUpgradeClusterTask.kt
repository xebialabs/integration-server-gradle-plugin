package ai.digital.integration.server.common.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.AwsEksHelper
import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.common.cluster.operator.GcpGkeHelper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.K8sSetup
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import ai.digital.integration.server.common.util.XlCliUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

abstract class OperatorBasedUpgradeClusterTask(@Input val productName: ProductName) : DefaultTask() {

    @Input
    val imageRepositoryName = project.objects.property<String>()

    @Input
    val imageTargetVersion = project.objects.property<String>()

    @Input
    val operatorBranch = project.objects.property<String>()

    @Input
    val opBlueprintBranch = project.objects.property<String>()

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)

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
        val crdName = operatorHelper.getKubectlHelper().getCrd("${productName.shortName}.digital.ai")
        val crName = operatorHelper.getKubectlHelper().getCr(crdName)
        val k8sSetup = if (productName == ProductName.DEPLOY) {
            XlCliUtil.XL_OP_MAPPING[DeployClusterUtil.getOperatorProviderName(project)]!!
        } else if (productName == ProductName.RELEASE) {
            XlCliUtil.XL_OP_MAPPING[ReleaseClusterUtil.getOperatorProviderName(project)]!!
        } else {
            throw IllegalArgumentException("Not supported product $productName")
        }

        val answersFile = when (k8sSetup) {
            K8sSetup.GoogleGKE.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_gke.yaml", "answers.yaml")
            }
            K8sSetup.Openshift.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_aws_openshift.yaml", "answers.yaml")
            }
            K8sSetup.AwsEKS.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_eks.yaml", "answers.yaml")
            }
            else -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers.yaml")
            }
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
                .replace("{{OPERATOR_ZIP_${productName.displayName.toUpperCase()}}}", operatorZip?.toAbsolutePath().toString())

        val answersFileTemplate = when (k8sSetup) {
            K8sSetup.GoogleGKE.toString() -> {
                answersFileTemplateTmp
                        .replace("{{K8S_TOKEN}}", (operatorHelper as GcpGkeHelper).getAccessToken())
            }
            K8sSetup.Openshift.toString() -> {
                answersFileTemplateTmp
                        .replace("{{K8S_TOKEN}}", (operatorHelper as AwsOpenshiftHelper).getOcApiServerToken())
            }
            K8sSetup.AwsEKS.toString() -> {
                val awsEksHelper = operatorHelper as AwsEksHelper
                answersFileTemplateTmp
                        .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                        .replace("{{CLUSTER_NAME}}", awsEksHelper.getProvider().clusterName.get())
                        .replace("{{AWS_ACCESS_KEY}}", awsEksHelper.getProvider().getAwsAccessKey())
                        .replace("{{AWS_ACCESS_SECRET}}", awsEksHelper.getProvider().getAwsSecretKey())
            }
            else -> {
                answersFileTemplateTmp
                        .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                        .replace("{{K8S_CLIENT_KEY}}", kubeContextInfo.tlsPrivateKey!!)
            }
        }

        answersFile.writeText(answersFileTemplate)
        return answersFile
    }

    private fun opUsingAnswersFile(operatorHelper: OperatorHelper, answersFile: File) {
        val opBlueprintPath = opBlueprintBranch.map { branch ->
            project.logger.lifecycle("Using xl-op-blueprints from branch $branch")
            GitUtil.checkout("xl-op-blueprints", Paths.get(operatorHelper.getProviderHomeDir()), branch).toFile()
        }

        project.logger.lifecycle("Applying prepared answers file ${answersFile.absolutePath}")
        XlCliUtil.xlOp(project,
                operatorHelper.getProfile().xlCliPath.get(),
                answersFile,
                File(operatorHelper.getProviderHomeDir()),
                opBlueprintPath.orNull)
    }

    private fun operatorBranchToOperatorZip(operatorHelper: OperatorHelper): Path? {
        operatorBranch.orNull.let { branch ->
            project.logger.lifecycle("Using xl-${productName.displayName}-kubernetes-operator from branch $branch")
            val operatorPath = GitUtil.checkout("xl-${productName.displayName}-kubernetes-operator",
                    Paths.get(operatorHelper.getProviderHomeDir()),
                    branch)
            val src = Paths.get(operatorPath.toAbsolutePath().toString(), operatorHelper.getProviderHomePath())
            val dest = Paths.get(operatorHelper.getProviderHomeDir(), "operator-${productName.displayName}-upgrade.zip")
            ant.withGroovyBuilder {
                "zip"("basedir" to src.toAbsolutePath().toString(),
                        "destfile" to dest.toAbsolutePath().toString())
            }
            return dest
        }
    }
}
