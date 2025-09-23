package ai.digital.integration.server.common.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.helm.AwsOpenshiftHelmHelper
import ai.digital.integration.server.common.cluster.operator.*
import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.K8sSetup
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.profiles.Profile
import ai.digital.integration.server.common.util.GitUtil
import ai.digital.integration.server.common.util.XlCliUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadXlCliDistTask
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class OperatorBasedUpgradeClusterTask(@Input val productName: ProductName) : DefaultTask() {

    @Input
    val imageRepositoryName = project.objects.property<String>()

    @Input
    val imageTargetVersion = project.objects.property<String>()

    @Input
    val deployUpgraderVersion = project.objects.property<String>().value(imageTargetVersion)

    @Input
    val useOperatorZip = project.objects.property<Boolean>().value(true)

    @Optional
    @Input
    val operatorBranch = project.objects.property<String>()

    @Optional
    @Input
    val opBlueprintBranch = project.objects.property<String>()

    @Optional
    @Input
    val keycloakUrl = project.objects.property<String>()

    private val runningTime = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())

    init {
        group = PluginConstant.PLUGIN_GROUP

        val upgradeTask = this
        project.afterEvaluate {

            val dependencies = mutableListOf<Any>(DownloadXlCliDistTask.NAME)

            if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent || ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
                val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)
                if (useOperatorZip.get() && operatorHelper.getProvider().operatorPackageVersion.isPresent) {
                    project.dependencies.add(
                        DeployConfigurationsUtil.OPERATOR_DIST,
                        "ai.digital.${operatorHelper.productName.displayName}.operator:${operatorHelper.getProviderHomePath()}:${operatorHelper.getProvider().operatorPackageVersion.get()}@zip"
                    )

                    val taskName = "downloadOperator${operatorHelper.getProviderHomePath()}"
                    val providerHomePath = operatorHelper.getProviderHomePath()
                    val task = project.tasks.register(taskName, Copy::class.java) {
                        from(project.zipTree(project.configurations.getByName(DeployConfigurationsUtil.OPERATOR_DIST).singleFile))
                        into(getUpgradeDir(operatorHelper).toFile().resolve(providerHomePath))
                    }
                    dependencies.add(task)
                }
            } else {
                project.logger.warn("Active provider name is not set - OperatorBasedUpgradeClusterTask")
            }
            upgradeTask.dependsOn(dependencies)
        }
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)

        val operatorZip = operatorBranchToOperatorZip(operatorHelper)
        launchCluster(operatorHelper)
        val answersFile = prepareAnswersFile(operatorHelper, operatorZip)
        opUsingAnswersFile(operatorHelper, answersFile)

        operatorHelper.waitForDeployment(operatorHelper.getProfile().ingressType.get(), operatorHelper.getProfile().deploymentTimeoutSeconds.get())
        operatorHelper.waitForMasterPods(operatorHelper.getProfile().deploymentTimeoutSeconds.get())
        operatorHelper.waitForWorkerPods(operatorHelper.getProfile().deploymentTimeoutSeconds.get())
        operatorHelper.waitForBoot(operatorHelper.getContextRoot(), operatorHelper.getFqdn())
    }

    private fun getUpgradeDir(operatorHelper: OperatorHelper): Path = Paths.get(operatorHelper.getProviderWorkDir(), runningTime)

    private fun getCrdGroupName(k8sSetup: String): String =
        if (k8sSetup == K8sSetup.Openshift.toString()) {
            "${productName.shortName}ocp.digital.ai"
        } else {
            "${productName.shortName}.digital.ai"
        }

    private fun prepareAnswersFile(operatorHelper: OperatorHelper, operatorZip: Path?): File {

        val clusterUtil = OperatorUtil(project)
        val server = clusterUtil.getOperatorServer()
        val operatorImage = operatorHelper.getOperatorImage() ?: getOperatorImage(operatorHelper)
        val namespace = operatorHelper.getProfile().namespace.getOrElse(Profile.DEFAULT_NAMESPACE_NAME)
        val k8sSetup = when (productName) {
            ProductName.DEPLOY -> {
                XlCliUtil.XL_OP_MAPPING[DeployClusterUtil.getOperatorProviderName(project)]!!
            }
            ProductName.RELEASE -> {
                XlCliUtil.XL_OP_MAPPING[ReleaseClusterUtil.getOperatorProviderName(project)]!!
            }
        }
        val crdName = operatorHelper.getKubectlHelper().getCrd(getCrdGroupName(k8sSetup))
        val crName = operatorHelper.getKubectlHelper().getCr(crdName)

        val targetFileName = "$runningTime/answers.yaml"
        val answersFile = when (k8sSetup) {
            K8sSetup.GoogleGKE.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_gke.yaml", targetFileName)
            }
            K8sSetup.Openshift.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_aws_openshift.yaml", targetFileName)
            }
            K8sSetup.AzureAKS.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_aks.yaml", targetFileName)
            }
            K8sSetup.AwsEKS.toString() -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers_eks.yaml", targetFileName)
            }
            else -> {
                operatorHelper.getTemplate("operator/xl-upgrade/${productName.displayName}/answers.yaml", targetFileName)
            }
        }
        project.logger.lifecycle("Preparing answers file ${answersFile.absolutePath}")

        val kubeContextInfo = operatorHelper.getCurrentContextInfo()

        val answersFileTemplateTmp = answersFile.readText(Charsets.UTF_8)
                .replace("{{CRD_NAME}}", crdName)
                .replace("{{CR_NAME}}", crName)
                .replace("{{USE_CUSTOM_NAMESPACE}}", (namespace != Profile.DEFAULT_NAMESPACE_NAME).toString())
                .replace("{{K8S_NAMESPACE}}", namespace)
                .replace("{{IS_CRD_REUSED}}", (namespace != Profile.DEFAULT_NAMESPACE_NAME).toString())
                .replace("{{K8S_SETUP}}", k8sSetup)
                .replace("{{OPERATOR_IMAGE}}", operatorImage)
                .replace("{{REPOSITORY_NAME}}", imageRepositoryName.get())
                .replace("{{IMAGE_TAG}}", imageTargetVersion.get())
                .replace("{{DEPLOY_VERSION_FOR_UPGRADER}}", server.version!!)
                .replace("{{USE_KEYCLOAK}}", keycloakUrl.map { StringUtils.isNotBlank(it) }.getOrElse(false).toString())
                .replace("{{KEYCLOAK_URL}}", keycloakUrl.getOrElse("null"))
                .replace("{{DEPLOY_VERSION_FOR_UPGRADER}}", deployUpgraderVersion.get())
                .replace("{{USE_OPERATOR_ZIP}}", (operatorZip != null).toString())
                .replace("{{OPERATOR_ZIP_${productName.displayName.uppercase()}}}", operatorZip?.toAbsolutePath()?.toString() ?: "null")

        val answersFileTemplate = when (k8sSetup) {
            K8sSetup.GoogleGKE.toString() -> {
                answersFileTemplateTmp
                        .replace("{{K8S_TOKEN}}", (operatorHelper as GcpGkeOperatorHelper).getAccessToken())
                    .replace("{{K8S_API_SERVER_URL}}", kubeContextInfo.apiServerURL!!)
            }
            K8sSetup.Openshift.toString() -> {
                val awsOpenshiftOperatorHelper = operatorHelper as AwsOpenshiftOperatorHelper
                answersFileTemplateTmp
                        .replace("{{K8S_TOKEN}}", awsOpenshiftOperatorHelper.getOcApiServerToken())
                        .replace("{{K8S_API_SERVER_URL}}", awsOpenshiftOperatorHelper.awsOpenshiftHelper.getApiServerUrl())
            }
            K8sSetup.AzureAKS.toString() -> {
                answersFileTemplateTmp
                    .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                    .replace("{{K8S_CLIENT_KEY}}", kubeContextInfo.tlsPrivateKey!!)
                    .replace("{{K8S_API_SERVER_URL}}", kubeContextInfo.apiServerURL!!)
            }
            K8sSetup.AwsEKS.toString() -> {
                val awsEksHelper = operatorHelper as AwsEksOperatorHelper
                answersFileTemplateTmp
                        .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                        .replace("{{CLUSTER_NAME}}", awsEksHelper.getProvider().clusterName.get())
                        .replace("{{AWS_ACCESS_KEY}}", awsEksHelper.getProvider().getAwsAccessKey())
                        .replace("{{AWS_ACCESS_SECRET}}", awsEksHelper.getProvider().getAwsSecretKey())
                        .replace("{{K8S_API_SERVER_URL}}", kubeContextInfo.apiServerURL!!)
            }
            else -> {
                answersFileTemplateTmp
                        .replace("{{K8S_CLIENT_CERT}}", kubeContextInfo.caCert!!)
                        .replace("{{K8S_CLIENT_KEY}}", kubeContextInfo.tlsPrivateKey!!)
                        .replace("{{K8S_API_SERVER_URL}}", kubeContextInfo.apiServerURL!!)
            }
        }

        answersFile.writeText(answersFileTemplate)
        return answersFile
    }

    private fun opUsingAnswersFile(operatorHelper: OperatorHelper, answersFile: File) {
        val opBlueprintPath = opBlueprintBranch.map { branch ->
            project.logger.lifecycle("Using xl-op-blueprints from branch $branch")
            GitUtil.checkout("xl-op-blueprints", getUpgradeDir(operatorHelper), branch).toFile()
        }

        launchCluster(operatorHelper)

        project.logger.lifecycle("Applying prepared answers file ${answersFile.absolutePath}")
        XlCliUtil.xlOp(project,
                answersFile,
                getUpgradeDir(operatorHelper).toFile(),
                opBlueprintPath.orNull)

        // copy CR file for reference
        FileUtils.copyFileToDirectory(
            File(getUpgradeDir(operatorHelper).toFile(), "xebialabs/dai-${productName.displayName}/dai${productName.displayName}_cr.yaml"),
            File(operatorHelper.getProviderWorkDir())
        )
        operatorHelper.createClusterMetadata()
    }

    private fun launchCluster(operatorHelper: OperatorHelper) {
        if (operatorHelper is AwsOpenshiftOperatorHelper) {
            operatorHelper.launchCluster()
        } else if (operatorHelper is AwsOpenshiftHelmHelper) {
            operatorHelper.launchCluster()
        }
    }

    private fun operatorBranchToOperatorZip(operatorHelper: OperatorHelper): Path? {
        if (useOperatorZip.get() && operatorBranch.isPresent) {
            project.logger.lifecycle("Using xl-${productName.displayName}-kubernetes-operator from branch ${operatorBranch.get()}")
            val operatorPath = GitUtil.checkout("xl-${productName.displayName}-kubernetes-operator",
                    getUpgradeDir(operatorHelper),
                    operatorBranch.get())
            val src = Paths.get(operatorPath.toAbsolutePath().toString(), operatorHelper.getProviderHomePath())
            return prepareOperatorZip(operatorHelper, src)
        } else if (useOperatorZip.get() && useOperatorZip.get() && operatorHelper.getProvider().operatorPackageVersion.isPresent) {
            project.logger.lifecycle("Downloading xl-${productName.displayName}-kubernetes-operator version ${operatorHelper.getProvider().operatorPackageVersion.get()}")
            val providerHomePath = operatorHelper.getProviderHomePath()
            return getUpgradeDir(operatorHelper).toFile().resolve(providerHomePath).toPath()
        } else if (useOperatorZip.get()) {
            val providerHomePath = operatorHelper.getProviderHomePath()
            project.logger.lifecycle("Using current xl-${productName.displayName}-kubernetes-operator")
            val operatorPath = getUpgradeDir(operatorHelper).toFile().resolve("xl-${productName.displayName}-kubernetes-operator")
            project.rootDir.resolve(providerHomePath).copyRecursively(operatorPath.resolve(providerHomePath), true)
            return prepareOperatorZip(operatorHelper, operatorPath.resolve(providerHomePath).toPath())
        } else {
            return null
        }
    }

    private fun prepareOperatorZip(operatorHelper: OperatorHelper, src: Path): Path {
        val newCrFilePath = File(src.toFile(), operatorHelper.OPERATOR_CR_VALUES_REL_PATH)
        if (!newCrFilePath.exists()) {
            throw IllegalArgumentException("No CR file from operator package in ${newCrFilePath.absolutePath}")
        }
        operatorHelper.updateCustomOperatorCrValues(newCrFilePath)

        val dest = Paths.get(getUpgradeDir(operatorHelper).toFile().absolutePath, "operator-${productName.displayName}-upgrade.zip")
        ant.withGroovyBuilder {
            "zip"("basedir" to src.toAbsolutePath().toString(),
                "destfile" to dest.toAbsolutePath().toString())
        }
        return dest
    }

    private fun getOperatorImage(operatorHelper: OperatorHelper): String {
        val operatorDeployment = project.rootDir.resolve(operatorHelper.getProviderHomePath()).resolve(operatorHelper.OPERATOR_DEPLOYMENT_PATH)
        operatorHelper.getReferenceCrValuesFile()
        return YamlFileUtil.readFileKey(operatorDeployment, "spec.template.spec.containers[1].image") as String
    }
}
